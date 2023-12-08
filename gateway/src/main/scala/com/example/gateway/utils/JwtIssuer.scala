package com.example.gateway.utils

import com.improving.iam._
import com.improving.config._
import com.improving.extensions.oidc.OIDCIdentity
import com.improving.utils.SystemClock

import cats.syntax.all._
import scala.concurrent.duration.FiniteDuration
import java.util.UUID

case class JwtIssuerConfig(
  tokenIssuerUrl: String,
  tokenValidDuration: FiniteDuration,
  defaultUserRole: String
)

object JwtIssuerConfig {

  /** [[ShowConfig]] instance for [[JwtIssuerConfig]] */
  implicit val showConfigForJwtIssuerConfig: ShowConfig[JwtIssuerConfig] =
    ShowConfig { cfg => printer =>
      printer
        .appendLine("JwtIssuerConfig")
        .indent
        .appendLine(s"Issuer URL:           ${cfg.tokenIssuerUrl}")
        .appendLine(s"Token Valid Duration: ${cfg.tokenValidDuration.toMinutes} mins")
        .appendLine(s"Default User Role:    ${cfg.defaultUserRole}")
        .outdent
        .newline
    }

  def fromConfig(config: Config, sectionName: Option[String] = None): Either[Throwable, JwtIssuerConfig] = {
    import readers._

    val tokenIssuerUrlReader     = getString("issuer-url")
    val tokenValidDurationReader = getFiniteDuration("token-valid-duration")
    val defaultUserRoleReader    = getString("default-user-role")

    val configReader = (
      tokenIssuerUrlReader,
      tokenValidDurationReader,
      defaultUserRoleReader
    ).mapN(JwtIssuerConfig.apply)

    for {
      configSection   <- sectionName.fold(ConfigReader.always(config))(getConfig).runToEither(config)
      jwtIssuerConfig <- configReader.runToEither(configSection)
    } yield jwtIssuerConfig
  }

}

object JwtIssuer {

  def apply(config: JwtIssuerConfig, algorithmWithKeys: AlgorithmWithKeys): JwtIssuer =
    new JwtIssuer(config, algorithmWithKeys)

}

final class JwtIssuer private (config: JwtIssuerConfig, algorithmWithKeys: AlgorithmWithKeys) {
  private val authTokenService = AuthTokenService(algorithmWithKeys)

  private val javaDuration = {
    val scalaDuration = config.tokenValidDuration
    java.time.Duration.of(scalaDuration.length, scalaDuration.unit.toChronoUnit)
  }

  def jwtToHttpCookie(jwt: String): String = {
    val maxAge = config.tokenValidDuration.toSeconds
    val secure = if (config.tokenIssuerUrl.startsWith("https")) "; Secure" else ""

    s"authToken=$jwt; Path=/; MaxAge=$maxAge$secure"
  }

  def createJwtFor(identity: OIDCIdentity): Either[Throwable, String] = {
    val nowInstant = SystemClock.currentInstant

    val authToken =
      AuthToken(
        UUID.randomUUID,
        config.tokenIssuerUrl,
        identity.id.toString,
        nowInstant.plus(javaDuration),
        nowInstant,
        nowInstant,
        Set(config.defaultUserRole)
      )

    authTokenService.encodeToken(authToken)
  }

  def decodeJwtToken(token: String): Either[Throwable, AuthToken] =
    authTokenService.validateAndExtractToken(token)

}
