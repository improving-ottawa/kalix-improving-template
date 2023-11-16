package com.example.gateway.api

import com.example.boundedContext.api._
import com.example.boundedContext.domain._
import com.example.gateway.HealthCheckResponse
import com.example.gateway.domain.{DoNothingTwiceCommand, DoNothingTwiceResponse}
import com.example.gateway.utils.ServiceOnlineUtil
import com.google.protobuf.empty.Empty
import com.typesafe.config.{Config, ConfigFactory}
import kalix.javasdk.impl.GrpcClients
import kalix.scalasdk.action.{Action, ActionCreationContext}
import org.slf4j.{Logger, LoggerFactory}

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class GatewayProxy(protected val creationContext: ActionCreationContext)
    extends GatewayProxyBase
    with Service1Proxy
    with Service2Proxy
    with LoginProxy
    with JwtAuthorization {

  final protected val system: akka.actor.ActorSystem                  = creationContext.materializer.system
  implicit final protected val materializer: akka.stream.Materializer = creationContext.materializer
  final protected val grpcClients                                     = GrpcClients(system)

  final protected val log: Logger           = LoggerFactory.getLogger(this.getClass)
  final protected val configuration: Config = ConfigFactory.load()

  // All gRPC clients
  final protected lazy val service1Client: Service1Service =
    grpcClients.getGrpcClient(classOf[Service1Service], "bounded-context")

  final protected lazy val service2Client: Service2Service =
    grpcClients.getGrpcClient(classOf[Service2Service], "bounded-context")

  protected val runningIntegrationTests: Boolean = {
    val sysProp = Option(System.getProperty("integration.test"))
    sysProp.exists(_.nonEmpty)
  }

  private val serviceOnline = ServiceOnlineUtil(creationContext.materializer.system)

  override def onlineCheck(empty: Empty): Action.Effect[Empty] =
    effects.reply(Empty()) // Simply reply online

  def healthCheck(empty: Empty): Action.Effect[HealthCheckResponse] =
    effects.asyncEffect(serviceOnline.healthChecks().map(effects.reply[HealthCheckResponse]))

  override def doNothingTwice(doNothingTwiceCommand: DoNothingTwiceCommand): Action.Effect[DoNothingTwiceResponse] =
    effects.asyncReply(for {
      _ <- service1Client.doNothing(DoNothingCommand1.defaultInstance)
      _ <- service2Client.doNothing(DoNothingCommand2.defaultInstance)
    } yield DoNothingTwiceResponse.defaultInstance)

}
