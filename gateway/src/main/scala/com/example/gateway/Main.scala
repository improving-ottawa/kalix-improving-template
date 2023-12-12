package com.example.gateway

import com.example.gateway.api._
import com.example.gateway.entity._
import com.example.gateway.middleware._
import com.example.gateway.utils._
import com.improving.iam._
import com.improving.extensions.oidc._
import com.improving.utils.{AsyncContext, StringPrinter}
import com.typesafe.config.ConfigFactory
import kalix.javasdk._
import kalix.scalasdk.action.ActionOptions
import kalix.scalasdk.{Kalix, WrappedKalix}
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

object Main {

  private val log = LoggerFactory.getLogger("com.example.gateway.Main")

  def createKalixForTest(): Kalix = {
    implicit val testEffect: OIDCClient.SupportedEffect[Future] =
      OIDCClient.SupportedEffect.testEffectForFuture

    val algorithmWithKeys: AlgorithmWithKeys = NoKeysPair

    val blankJwtIssuerConfig = JwtIssuerConfig(
      tokenIssuerUrl = "http://localhost:9000",
      tokenValidDuration = FiniteDuration(1, "second"),
      defaultUserRole = "None"
    )

    val blankIdentityConfig = OIDCIdentityServiceConfig(
      providerCallback = sttp.model.Uri.unsafeParse("http://localhost:9000/oidc/callback"),
      providers = Map.empty
    )

    val jwtIssuer       = JwtIssuer(blankJwtIssuerConfig, algorithmWithKeys)
    val identityService = OIDCIdentityService[Future](blankIdentityConfig, algorithmWithKeys)

    KalixFactory.withComponents(
      new LoginTokenService(_, algorithmWithKeys),
      new UserEntity(_),
      new AuthenticationServiceAction(identityService, jwtIssuer, _),
      new GatewayProxy(_, jwtIssuer)
    )
  }

  def createKalix(
    keyLoaderConfig: KeyLoaderConfig,
    identityServiceConfig: OIDCIdentityServiceConfig,
    jwtIssuerConfig: JwtIssuerConfig
  )(implicit asyncContext: AsyncContext): Kalix = {
    val algorithmWithKeys = KeyLoader
      .load(keyLoaderConfig)
      .fold(
        error => {
          log.warn("No KeyPair loaded due to an error, all cryptographic functions/behavior will fail!", error)
          NoKeysPair
        },
        identity
      )

    val jwtIssuer       = JwtIssuer(jwtIssuerConfig, algorithmWithKeys)
    val identityService = OIDCIdentityService[Future](identityServiceConfig, algorithmWithKeys)

    val authServiceProvider = AuthenticationServiceActionProvider(
      ctx => new AuthenticationServiceAction(identityService, jwtIssuer, ctx),
      ActionOptions.defaults.withForwardHeaders(Set("Authorization", "Cookie", "X-CSRF-Token"))
    )

    val gatewayAuthedProvider = AuthenticatedActionProvider(
      GatewayProxyProvider(new GatewayProxy(_, jwtIssuer))
    )

    printServiceConfig(keyLoaderConfig, identityServiceConfig, jwtIssuerConfig)

    Kalix()
      .register(authServiceProvider)
      .register(gatewayAuthedProvider)
      .register(LoginTokenServiceProvider(new LoginTokenService(_, algorithmWithKeys)))
      .register(UserEntityProvider(new UserEntity(_)))
  }

  def main(args: Array[String]): Unit = {
    log.info("Starting the Kalix service")

    @inline def reportError(name: String)(error: Throwable): Nothing = {
      log.error(s"Error loading configuration for: $name", error)
      throw error
    }

    val systemConfig = ConfigFactory.load()

    val keyLoaderConfig = KeyLoaderConfig
      .fromConfig(systemConfig, Some("com.example.gateway.key-loader"))
      .fold(reportError("KeyLoaderConfig"), identity)

    val jwtIssuerConfig = JwtIssuerConfig
      .fromConfig(systemConfig, Some("com.example.gateway.jwt"))
      .fold(reportError("JwtIssuer"), identity)

    val identityServiceConfig = OIDCIdentityServiceConfig
      .fromConfig(systemConfig, Some("com.example.gateway.identity"))
      .fold(reportError("OIDCIdentityService"), identity)

    val actorSystem  = OpenKalixRunner.createActorSystem("gateway-kalix")
    val asyncContext = AsyncContext.akkaFrom(actorSystem)
    val rawKalix     = createKalix(keyLoaderConfig, identityServiceConfig, jwtIssuerConfig)(asyncContext)
    val kalix        = WrappedKalix(rawKalix)

    kalix.createRunnerVia(actorSystem).run()
  }

  private def printServiceConfig(
    keyLoaderConfig: KeyLoaderConfig,
    identityServiceConfig: OIDCIdentityServiceConfig,
    jwtIssuerConfig: JwtIssuerConfig
  ): Unit = {
    val configPrinter = StringPrinter(indentSize = 2)
      .appendLine("Gateway configuration:")
      .indent
      .appendConfig(keyLoaderConfig)
      .appendConfig(jwtIssuerConfig)
      .appendConfig(identityServiceConfig)
      .outdent

    log.info(configPrinter.result)
  }

}
