package com.example.gateway.utils

import com.improving.iam._
import com.improving.config._
import com.improving.utils.{Base64String, SystemClock}
import cats.syntax.all._
import com.improving.extensions.identity.UserIdentity
import com.improving.extensions.identity.oidc.OIDCIdentity
import sttp.model.Uri

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

  /* Typed / specific errors */

  case class CsrfTokenMismatch(fromJwt: Base64String, fromHeader: Base64String)
      extends Error(
        s"CSRF verification failed, received different CSRF tokens from JWT ($fromJwt) and HTTP header ($fromHeader)."
      )

  case class MissingCsrfToken(authToken: AuthToken)
      extends Error(s"Authorization token missing `csrf_token` in claims: $authToken")

}

final class JwtIssuer private (config: JwtIssuerConfig, algorithmWithKeys: AlgorithmWithKeys) {
  private val authTokenService = AuthTokenService(algorithmWithKeys)

  private val jwtCookieDomain = Uri
    .unsafeParse(config.tokenIssuerUrl)
    .authority
    .map(_.host)
    .getOrElse("localhost")

  private val javaDuration = {
    val scalaDuration = config.tokenValidDuration
    java.time.Duration.of(scalaDuration.length, scalaDuration.unit.toChronoUnit)
  }

  def jwtTokenValidDuration: Long = config.tokenValidDuration.toSeconds

  def jwtToHttpCookie(jwt: String): String = {
    val maxAge = config.tokenValidDuration.toSeconds
    val secure = if (config.tokenIssuerUrl.startsWith("https")) "; Secure" else ""

    s"authToken=$jwt; Path=/; Domain=$jwtCookieDomain; SameSite=Lax; Max-Age=$maxAge$secure"
  }

  def createJwtFor(identity: UserIdentity, csrfToken: Base64String): Either[Throwable, (String, Long)] = {
    val nowInstant       = SystemClock.currentInstant
    val tokenExpiration  = nowInstant.plus(javaDuration)
    val additionalClaims = Map("csrf_token" -> csrfToken.toString)

    val authToken =
      AuthToken(
        UUID.randomUUID,
        config.tokenIssuerUrl,
        identity.id.toString,
        tokenExpiration,
        nowInstant,
        nowInstant,
        Set(config.defaultUserRole),
        additionalClaims = additionalClaims
      )

    authTokenService.encodeToken(authToken).map(jwt => (jwt, tokenExpiration.getEpochSecond))
  }

  def decodeJwtToken(token: String): Either[Throwable, AuthToken] =
    authTokenService.validateAndExtractToken(token)

  def validateAndExtract(jwt: String, csrfToken: Base64String): Either[Throwable, AuthToken] = {
    @inline def extractJwtAuthToken(authToken: AuthToken) =
      authToken.additionalClaims
        .get("csrf_token")
        .map(Base64String.fromBase64String(_).leftMap(_ => JwtIssuer.MissingCsrfToken(authToken)))
        .getOrElse(Left(JwtIssuer.MissingCsrfToken(authToken)))

    @inline def verifyCsrfTokens(fromJwt: Base64String) =
      if (csrfToken == fromJwt) Right(())
      else Left(JwtIssuer.CsrfTokenMismatch(fromJwt, csrfToken))

    for {
      authToken    <- authTokenService.validateAndExtractToken(jwt)
      jwtCsrfToken <- extractJwtAuthToken(authToken)
      _            <- verifyCsrfTokens(jwtCsrfToken)
    } yield authToken
  }

}
