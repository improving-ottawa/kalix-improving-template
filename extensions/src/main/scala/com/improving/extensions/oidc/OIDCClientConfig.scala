package com.improving.extensions.oidc

import com.improving.config._

import cats.syntax.all._

import java.time.Duration

/**
  * Configuration for the OpenID Connect provider client.
  *
  * @param clientId
  *   The client ID for this ODIC client.
  * @param discoveryUri
  *   The public discovery Uri (typically ends with `/.well-known/openid-configuration`)
  * @param publicUrl
  *   The public facing base URL for the access management server (i.e. "this server").
  * @param clientSecret
  *   The client secret for this OIDC client
  * @param useNonce
  *   Specifies whether to send nonce when interacting with the OIDC provider.
  * @param sessionTimeToLive
  *   Specifies how long a login session will survive in the session cache before being expired.
  * @param clockSkew
  *   Sets the allowable clock skew (difference) between the OIDC provider and this client.
  * @param codeFlowParams
  *   An optional map of (String, String) query-string parameters to pass along to the OIDC client.
  */
case class OIDCClientConfig(
  clientId: String,
  discoveryUri: String,
  publicUrl: String,
  clientSecret: String,
  useNonce: Boolean = true,
  sessionTimeToLive: Duration = Duration.ofSeconds(120),
  clockSkew: Duration = Duration.ofSeconds(60),
  codeFlowParams: Map[String, String] = Map.empty
)

object OIDCClientConfig {

  /** Reads an [[OIDCClientConfig]] configuration from a typelevel [[Config config]]. */
  def fromConfig(config: Config, configSectionName: Option[String] = None): Either[Throwable, OIDCClientConfig] = {
    import readers._

    val clientId      = getString("client-id")
    val discoveryUri  = getString("discovery-uri")
    val publicUrl     = getString("public-url")
    val clientSecret  = getString("client-secret")
    val useNonce      = getBoolean("use-nonce").withDefault(true)
    val sessionTTL    = getJavaDuration("session-time-to-live").withDefault(Duration.ofSeconds(120))
    val clockSkew     = getJavaDuration("clock-skew").withDefault(Duration.ofSeconds(90))
    val codeFlowParms = getStringsMap("code-flow-params").withDefault(Map.empty[String, String])

    val readConfig = (
      clientId,
      discoveryUri,
      publicUrl,
      clientSecret,
      useNonce,
      sessionTTL,
      clockSkew,
      codeFlowParms
    ).mapN(OIDCClientConfig.apply)

    for {
      configSection <- configSectionName.fold(ConfigReader.always(config))(getConfig).runToEither(config)
      clientConfig  <- readConfig.runToEither(configSection)
    } yield clientConfig
  }

}
