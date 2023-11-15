package com.example.gateway

import com.example.gateway.domain.{DoNothingTwiceCommand, DoNothingTwiceEvent}
import com.example.service1.Service1
import com.example.service1.domain.{DoNothingCommand => Service1DoNothingCommand}
import com.example.service2.Service2
import com.example.service2.domain.{DoNothingCommand => Service2DoNothingCommand}

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

  val service1: Service1 = creationContext.getGrpcClient(
    classOf[Service1],
    config.getString("com.improving.template.gateway.service1.grpc-client-name")
  )

  val service2: Service2 = creationContext.getGrpcClient(
    classOf[Service2],
    config.getString("com.improving.template.gateway.service2.grpc-client-name")
  )

  override def doNothingTwice(doNothingTwiceCommand: DoNothingTwiceCommand): Action.Effect[DoNothingTwiceEvent] = {
    effects.asyncReply {
      for {
        _ <- service1.doNothing(Service1DoNothingCommand())
        _ <- service2.doNothing(Service2DoNothingCommand())
      } yield DoNothingTwiceEvent()
    }
  }
}
