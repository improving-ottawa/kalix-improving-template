package com.improving.testkit.internal

import com.improving.testkit._

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import kalix.javasdk.impl.GrpcClients
import org.slf4j.LoggerFactory

import scala.concurrent._
import scala.concurrent.duration._

private[testkit] final class TestKitImpl private() extends KalixServiceManager
  with TestKitBuilder with BuildableTestKitBuilder with IntegrationTestKitApi {
  import TestKitImpl._

  private[this] var currentState: State = BuildingState

  private val log = LoggerFactory.getLogger("com.improving.testkit.IntegrationTestKit")

  def withKalixService(service: KalixService): BuildableTestKitBuilder = {
    registerService(service.toEntry)
    this
  }

  def buildKit: IntegrationTestKitApi =
    currentState match {
      case BuildingState =>
        log.info("Starting TestKit...")
        synchronized {
          startAllInstances()

          val clientsConfig = getClientsConfig
          val renderOptions = ConfigRenderOptions.concise().setJson(false).setFormatted(true)
          val configText = clientsConfig.root().render(renderOptions)
          log.info(s"Generated the following gRPC clients configuration:\n$configText")

          val systemConfig = clientsConfig.withFallback(testKitSystemConfig)
          val actorSystem = ActorSystem("IntegrationTestKitSystem", systemConfig)
          val materializer = Materializer(actorSystem)
          currentState = RunningState(actorSystem, materializer)(actorSystem.dispatcher)
          log.info("Testkit started.")
          this
        }

      case _ => throw new IntegrationTestError("Testkit already running!")
    }

  /* IntegrationTestKitApi Implementation */

  def system: ActorSystem = currentState.system

  def materializer: Materializer = currentState.materializer

  implicit def executionContext: ExecutionContext = currentState.executionContext

  def stop(): Unit =
    currentState match {
      case RunningState(system, _) =>
        log.info("Stopping TestKit...")
        synchronized {
          stopAllServices()
          Await.result(system.terminate(), FiniteDuration(30, "seconds"))
          currentState = BuildingState
          log.info("TestKit stopped.")
        }

      case _ => ()
    }

  def getPortForService(serviceName: String): Option[Int] =
    if (hasRegisteredService(serviceName)) Some(kalixProxyPortMappings(serviceName)) else None

  def getGrpcClient[T](clientClass: Class[T], serviceName: String): T =
    getPortForService(serviceName) match {
      case Some(port) => currentState.grpcClients.getGrpcClient(clientClass, serviceName, port)
      case None       => throw new IntegrationTestError(s"No registered service for name: $serviceName")
    }

}

private[testkit] object TestKitImpl {
  private final val testKitSystemConfig = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = true")

  private[internal] def emptyBuilder: TestKitBuilder = new TestKitImpl()

  private sealed trait State {
    def system: ActorSystem

    def materializer: Materializer

    def grpcClients: GrpcClients

    implicit def executionContext: ExecutionContext
  }

  private case object BuildingState extends State {
    def system: ActorSystem = throw new RuntimeException("TestKit not built")
    def materializer: Materializer = throw new RuntimeException("TestKit not built")
    def grpcClients: GrpcClients = throw new RuntimeException("TestKit not built")
    implicit def executionContext: ExecutionContext = ExecutionContext.global
  }

  private case class RunningState(system: ActorSystem, materializer: Materializer)
                                 (implicit val executionContext: ExecutionContext) extends State {
    val grpcClients: GrpcClients = GrpcClients(system)
  }

}
