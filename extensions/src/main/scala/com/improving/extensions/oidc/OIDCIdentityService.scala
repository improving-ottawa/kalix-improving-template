package com.improving.extensions.oidc

import com.improving.iam.AlgorithmWithKeys
import com.improving.config._
import com.improving.utils.SystemClock

import cats._
import cats.effect.SyncIO
import cats.syntax.all._
import sttp.model.Uri

import java.time.{Duration, Instant}

case class OIDCIdentityServiceConfig(
  providerCallback: Uri,
  providers: Map[String, OIDCClientConfig]
)

object OIDCIdentityServiceConfig {

  /** [[ShowConfig]] instance for [[OIDCIdentityServiceConfig]] */
  implicit val showConfigForOIDCIdentityServiceConfig: ShowConfig[OIDCIdentityServiceConfig] =
    ShowConfig { cfg => printer =>
      printer
        .appendLine("OIDC Identity Service:")
        .indent
        .appendLine(s"Provider Callback URI: ${cfg.providerCallback}")
        .appendLine("Providers:")
        .indent
        .printEach(cfg.providers) { case (name, provider) =>
          providerPrinter =>
            providerPrinter
              .appendLine(name)
              .indent
              .appendLine(s"Client ID:         ${provider.clientId}")
              .append("Client Secret:     ")
              .ifThen(provider.clientSecret.nonEmpty)(_.appendLine("{confidential}"))(_.appendLine("Not Set"))
              .appendLine(s"Discovery URI:     ${provider.discoveryUri}")
              .appendLine(s"Use Nonce:         ${provider.useNonce}")
              .appendLine(s"Session TTL:       ${provider.sessionTimeToLive.toSeconds} seconds")
              .appendLine(s"Clock Skew:        ${provider.clockSkew.toSeconds} seconds")
              .append("Additional Params: ")
              .ifThen(provider.codeFlowParams.isEmpty)(_.appendLine("None"))(
                _.newline.indent
                  .printEach(provider.codeFlowParams) { case (key, value) =>
                    _.appendLine(s"$key: $value")
                  }
                  .outdent
              )
              .outdent
        }
        .outdent
        .outdent
        .newline
    }

  def fromConfig(config: Config, sectionName: Option[String] = None): Either[Throwable, OIDCIdentityServiceConfig] = {
    import readers._

    val callbackReader  = getString("callback-uri ").map(sttp.model.Uri.unsafeParse)
    val providersReader = getKeyedConfigMap("providers")
      .flatMapF { cfgMap =>
        cfgMap.toSeq.traverse { case (key, cfg) =>
          SyncIO
            .fromEither(OIDCClientConfig.fromConfig(cfg))
            .map(config => (key, config))
        }
      }
      .map(_.toMap)

    val serviceConfigReader = (
      callbackReader,
      providersReader
    ).mapN(OIDCIdentityServiceConfig.apply)

    val configSection = sectionName.fold(ConfigReader.always(config))(getConfig).runToEither(config)
    configSection.flatMap(serviceConfigReader.runToEither)
  }

}

object OIDCIdentityService {
  import OIDCClient.SupportedEffect

  /* Constants */
  final val sessionExpiration = Duration.ofMinutes(3)

  /* Applicatives (constructors) */

  def apply[F[_] : SupportedEffect](config: OIDCIdentityServiceConfig, awk: AlgorithmWithKeys): OIDCIdentityService[F] =
    new OIDCIdentityService[F](config, awk)

  def apply[F[_] : SupportedEffect](
    algorithmWithKeys: AlgorithmWithKeys,
    providerCallback: Uri,
    providers: Map[String, OIDCClientConfig]
  ): OIDCIdentityService[F] =
    new OIDCIdentityService[F](OIDCIdentityServiceConfig(providerCallback, providers), algorithmWithKeys)

  /* Typed / specific errors */
  case class InvalidProviderIdError(providerId: String) extends Error(s"Invalid `providerId` supplied: $providerId")

  case class SessionTimeout(issuedAt: Instant, elapsed: Duration)
      extends Error(s"Session state issued at `$issuedAt` has timed-out after $elapsed")

}

final class OIDCIdentityService[F[_] : OIDCClient.SupportedEffect] private (
  config: OIDCIdentityServiceConfig,
  algorithmWithKeys: AlgorithmWithKeys
) {
  import OIDCIdentityService._

  private val effect       = implicitly[OIDCClient.SupportedEffect[F]]
  private val tokenService = OIDCStateService(algorithmWithKeys)

  implicit private val F: MonadThrow[F] = effect.monadThrow

  def beginAuthorizationCodeFlow(state: OIDCState): F[Uri] = {
    @inline def generateRedirectUri(providerConfig: OIDCClientConfig): F[Uri] = {
      val stateToken = tokenService.signToken(state)
      OIDCClient[F](providerConfig, config.providerCallback).beginAuthentication(stateToken)
    }

    config.providers.get(state.providerId) match {
      case None                 => F.raiseError(InvalidProviderIdError(state.providerId))
      case Some(providerConfig) => generateRedirectUri(providerConfig)
    }
  }

  def completeAuthorizationCodeFlow(code: String, stateToken: String): F[(OIDCIdentity, OIDCState)] = {
    @inline def checkSessionExpiration(state: OIDCState, providerConfig: OIDCClientConfig): F[Unit] = {
      val nowInstant        = SystemClock.currentInstant
      val elapsedTime       = Duration.between(state.issuedAt, nowInstant)
      val sessionTimeToLive = providerConfig.sessionTimeToLive

      if (sessionTimeToLive.minus(elapsedTime).isNegative)
        F.raiseError(SessionTimeout(state.issuedAt, elapsedTime))
      else
        F.unit
    }

    @inline def getProviderConfig(providerId: String): F[OIDCClientConfig] =
      config.providers.get(providerId) match {
        case Some(provider) => F.pure(provider)
        case None           => F.raiseError(InvalidProviderIdError(providerId))
      }

    for {
      state          <- F.fromEither(tokenService.parseSessionToken(stateToken))
      providerConfig <- getProviderConfig(state.providerId)
      _              <- checkSessionExpiration(state, providerConfig)
      client         <- F.pure(OIDCClient[F](providerConfig, config.providerCallback))
      identity       <- client.completeAuthentication(code)
    } yield (identity, state)
  }

}
