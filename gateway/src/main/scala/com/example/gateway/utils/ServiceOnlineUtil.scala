package com.example.gateway.utils

import com.example.gateway._
import com.example.boundedContext
import com.example.utils.SystemClock

import akka.actor.{ActorSystem, ClassicActorSystemProvider}
import com.example.common.{PingThroughRequest, PingThroughResponse}
import com.example.utils.SystemClock
import kalix.javasdk.impl.GrpcClients

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.Try
import java.time.{Duration, Instant}
import java.util.UUID

final class ServiceOnlineUtil private (actorSystem: ActorSystem) {
  import ServiceOnlineUtil._

  implicit private val executionContext: ExecutionContext = actorSystem.dispatcher

  /* Health checks */

  def healthChecks(): Future[HealthCheckResponse] = {
    def parCheckServiceStatuses(id: UUID): Future[List[ServiceStatus]] =
      Future.sequence(
        healthCheckEndpoints.map { case (serviceName, endpoint) =>
          serviceName match {
            case "bounded-context" =>
              checkServiceStatus[boundedContext.api.PingPongService](serviceName, endpoint, id, _.pingThrough)
            case unknown           => Future.failed(new RuntimeException(s"Unknown Kalix service: $unknown"))
          }
        }.toList
      )

    for {
      correlationId <- Future.successful(UUID.randomUUID())
      startTime     <- Future.fromTry(Try(SystemClock.currentInstant))
      statuses      <- parCheckServiceStatuses(correlationId)
      endTime       <- Future.fromTry(Try(SystemClock.currentInstant))
    } yield {
      val isHealthy       = statuses.forall(_.isOnline)
      val healthCheckTime = finiteDurationBetween(startTime, endTime)

      HealthCheckResponse(
        correlationId = correlationId,
        healthCheckTime = healthCheckTime,
        isHealthy = isHealthy,
        serviceStatuses = statuses
      )
    }
  }

  /* Utility functions */

  private def checkServiceStatus[T : ClassTag](
    serviceName: String,
    endpoint: ServiceMapping,
    correlationId: UUID,
    selector: T => PingThroughRequest => Future[PingThroughResponse]
  ): Future[ServiceStatus] = {
    val (hostingService, grpcPort) = (
      endpoint.hostingService,
      endpoint.grpcPort
    )

    val genRequest: Instant => PingThroughRequest =
      startTime =>
        PingThroughRequest(
          correlationId = correlationId,
          startTime = startTime,
          traces = List.empty
        )

    val checkProgram = for {
      client    <- tryCreateGrpcClient[T](hostingService, grpcPort)
      startTime <- Future.fromTry(Try(SystemClock.currentInstant))
      request   <- Future.successful(genRequest(startTime))
      _         <- selector(client)(request)
      endTime   <- Future.fromTry(Try(SystemClock.currentInstant))
      rtTime    <- Future.successful(finiteDurationBetween(startTime, endTime))
    } yield {
      ServiceStatus(
        serviceName = s"$hostingService.$serviceName",
        serviceInternalPort = grpcPort,
        isOnline = true,
        roundTripTime = Some(rtTime)
      )
    }

    checkProgram.recover { case _ =>
      ServiceStatus(
        serviceName = s"$hostingService.$serviceName",
        serviceInternalPort = grpcPort,
        isOnline = false, // prefer explicit field initialization
        roundTripTime = None
      )
    }
  }

  private def tryCreateGrpcClient[T : ClassTag](hostingService: String, grpcPort: Int = 9000): Future[T] =
    Future.fromTry(
      Try {
        val clientClass = implicitly[ClassTag[T]].runtimeClass
        val grpcClient  =
          if (localServiceEndpoints)
            GrpcClients(actorSystem).getGrpcClient(clientClass, hostingService, grpcPort)
          else
            GrpcClients(actorSystem).getGrpcClient(clientClass, hostingService)

        grpcClient.asInstanceOf[T]
      }
    )

  private def finiteDurationBetween(start: Instant, end: Instant): FiniteDuration = {
    val durBetween = Duration.between(start, end)
    FiniteDuration(durBetween.toMillis, MILLISECONDS)
  }

}

object ServiceOnlineUtil {

  // Controls how service endpoints are resolved. `false` is let Kalix do it, and `true` is manually specify.
  private lazy val localServiceEndpoints: Boolean = {
    import com.typesafe.config.ConfigFactory
    try {
      val config = ConfigFactory.load()
      if (config.hasPath("com.example.gateway.health-check.use-local")) {
        config.getBoolean("com.example.gateway.health-check.use-local")
      } else false
    } catch {
      case scala.util.control.NonFatal(_) =>
        // Default to "let Kalix figure it out"
        false
    }
  }

  def apply(actorSystemProvider: ClassicActorSystemProvider): ServiceOnlineUtil =
    new ServiceOnlineUtil(actorSystemProvider.classicSystem)

  // Note: Add other deployed services to this list (and to the public `healthChecks()` function above).
  final val healthCheckEndpoints = Map(
    "bounded-context" -> ServiceMapping("bounded-context", 9001),
  )

  final case class ServiceMapping(hostingService: String, grpcPort: Int)

}
