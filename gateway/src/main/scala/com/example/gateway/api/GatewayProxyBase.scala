package com.example.gateway.api

import com.example.boundedContext.api._
import com.example.service3.api._

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.Config
import kalix.javasdk.impl.GrpcClients
import org.slf4j.Logger

trait GatewayProxyBase extends AbstractGatewayProxy {
  protected def system: ActorSystem
  implicit protected def materializer: Materializer

  protected def log: Logger
  protected def configuration: Config
  protected def grpcClients: GrpcClients

  protected def cartService: ShoppingCarts
  protected def ordersService: Orders
  protected def productsService: Products

  protected def runningIntegrationTests: Boolean
}
