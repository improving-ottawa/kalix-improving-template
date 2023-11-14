package com.example.gateway

import com.typesafe.config.{Config, ConfigFactory}
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext
import org.slf4j.LoggerFactory

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class GatewayAction(creationContext: ActionCreationContext) extends AbstractGatewayAction {

  private val log = LoggerFactory.getLogger(this.getClass)

  lazy val config: Config = ConfigFactory.load()

  val service1: Service1Client = creationContext.getGrpcClient(
    classOf[Service1Client],
    config.getString(
      "com.example.template.gateway.service1.grpc-client-name"
    )
  )

  val service2: Service2Client = creationContext.getGrpcClient(
    classOf[Service2Client],
    config.getString(
      "com.example.template.gateway.service2.grpc-client-name"
    )
  )

  override def doNothingTwice(doNothingTwiceCommand: DoNothingTwiceCommand): Action.Effect[DoNothingTwiceEvent] = {
    effects.asyncReply {
      for {
        _ <- service1.doNothing().invoke(DoNothingCommand())
        _ <- service2.doNothing().invoke(DoNothingCommand())
      } yield DoNothingTwiceEvent()
    }
  }
}
