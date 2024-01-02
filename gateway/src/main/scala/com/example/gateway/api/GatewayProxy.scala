package com.example.gateway.api

import com.example.gateway.HealthCheckResponse
import com.example.gateway.domain._
import com.example.gateway.utils.{JwtIssuer, ServiceOnlineUtil}
import com.example.service3.api._

import com.google.protobuf.empty.Empty
import com.typesafe.config.{Config, ConfigFactory}
import kalix.javasdk.impl.GrpcClients
import kalix.scalasdk.action.{Action, ActionCreationContext}
import org.slf4j.{Logger, LoggerFactory}

// This class was initially generated based on the .proto definition by Kalix tooling.

class GatewayProxy(protected val creationContext: ActionCreationContext, protected val jwtIssuer: JwtIssuer)
  extends GatewayProxyBase
    with CartProxy
    with OrderProxy
    with ProductsProxy
    with UserProxy {

  final protected val system: akka.actor.ActorSystem                  = creationContext.materializer.system
  implicit final protected val materializer: akka.stream.Materializer = creationContext.materializer
  final protected val grpcClients                                     = GrpcClients(system)

  final protected val log: Logger           = LoggerFactory.getLogger(this.getClass)
  final protected val configuration: Config = ConfigFactory.load()

  // All gRPC clients
  final protected lazy val cartService: ShoppingCarts =
    grpcClients.getGrpcClient(classOf[ShoppingCarts], "bounded-context")

  final protected lazy val ordersService: Orders =
    grpcClients.getGrpcClient(classOf[Orders], "bounded-context")

  final protected lazy val productsService: Products =
    grpcClients.getGrpcClient(classOf[Products], "bounded-context")

  protected val runningIntegrationTests: Boolean = {
    val sysProp = Option(System.getProperty("integration.test"))
    sysProp.exists(_.nonEmpty)
  }

  private val serviceOnline = ServiceOnlineUtil(creationContext.materializer.system)

  override def onlineCheck(empty: Empty): Action.Effect[Empty] =
    effects.reply(Empty()) // Simply reply online

  def healthCheck(empty: Empty): Action.Effect[HealthCheckResponse] =
    effects.asyncEffect(serviceOnline.healthChecks().map(effects.reply[HealthCheckResponse]))

  override def completeLogin(request: CompleteLoginRequest): Action.Effect[CompleteLoginResponse] =
    effects.forward(components.authenticationServiceAction.oidcCompleteLogin(request))

}
