package com.example.gateway.api

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.example.boundedContext.api._
import com.example.service3.api.Service3
import com.typesafe.config.Config
import kalix.javasdk.impl.GrpcClients
import org.slf4j.Logger

trait GatewayProxyBase extends AbstractGatewayProxy {
  protected def system: ActorSystem
  implicit protected def materializer: Materializer

  protected def log: Logger
  protected def configuration: Config
  protected def grpcClients: GrpcClients

  protected def service1Client: Service1Service
  protected def service2Client: Service2Service
  protected def service3Client: Service3
  protected def runningIntegrationTests: Boolean
}
