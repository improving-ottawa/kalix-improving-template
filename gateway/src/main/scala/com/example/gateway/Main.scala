package com.example.gateway

import com.example.gateway.api._
import com.example.gateway.utils._
import com.improving.iam._
import com.improving.extensions.oidc._
import com.improving.utils.AsyncContext

import com.typesafe.config.ConfigFactory
import kalix.javasdk._
import kalix.scalasdk.{Kalix, WrappedKalix}
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

object Main {

  private val log = LoggerFactory.getLogger("com.example.template.Main")

  def createKalix(
    keyLoaderConfig: KeyLoaderConfig,
    identityServiceConfig: OIDCIdentityServiceConfig,
    jwtIssuerConfig: JwtIssuerConfig)(
    implicit asyncContext: AsyncContext
  ): Kalix = {
    val algorithmWithKeys = KeyLoader.load(keyLoaderConfig).fold(throw _, identity)

    val jwtIssuer = JwtIssuer(jwtIssuerConfig, algorithmWithKeys)
    val identityService = OIDCIdentityService[Future](identityServiceConfig, algorithmWithKeys)

    KalixFactory.withComponents(
      new LoginTokenService(_, algorithmWithKeys),
      new AuthenticationServiceAction(identityService, jwtIssuer, _),
      new GatewayProxy(_)
    )
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

    val actorSystem = OpenKalixRunner.createActorSystem("gateway-kalix")
    val asyncContext = AsyncContext.akkaFrom(actorSystem)
    val rawKalix = createKalix(keyLoaderConfig, identityServiceConfig, jwtIssuerConfig)(asyncContext)
    val kalix = WrappedKalix(rawKalix)

    kalix.createRunnerVia(actorSystem).run()
  }

}
