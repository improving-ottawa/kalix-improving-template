package com.example.gateway

import com.example.gateway.api.{GatewayProxy, KalixFactory, LoginTokenService}
import com.improving.iam._
import kalix.scalasdk.Kalix
import org.slf4j.LoggerFactory

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

object Main {

  private val log = LoggerFactory.getLogger("com.example.template.Main")

  private def keyLoaderConfig(): KeyLoaderConfig = ???

  def createKalix(): Kalix = {
    val algorithmWithKeys: AlgorithmWithKeys = NoKeysPair
    // KeyLoader.load(keyLoaderConfig()).fold(throw _, identity)

    // The KalixFactory automatically registers any generated Actions, Views or Entities,
    // and is kept up-to-date with any changes in your protobuf definitions.
    // If you prefer, you may remove this and manually register these components in a
    // `Kalix()` instance.
    KalixFactory.withComponents(
      new LoginTokenService(_, algorithmWithKeys),
      new GatewayProxy(_)
    )
  }

  def main(args: Array[String]): Unit = {
    log.info("starting the Kalix service")
    createKalix().start()
  }

}
