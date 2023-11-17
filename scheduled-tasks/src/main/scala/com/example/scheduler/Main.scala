package com.example.scheduler

import com.example.scheduler.api.TaskTrackerServiceImpl
import com.example.scheduler.entity.TaskTrackerEntity
import com.example.scheduler.services.DoNothing1ServiceImpl
import com.example.utils.FutureUtils.RetrySettings
import com.example.scheduler.services._
import com.example.utils.FutureUtils
import com.google.protobuf.empty.Empty
import kalix.scalasdk.{Kalix, WrappedKalix}
import kalix.javasdk.impl.ProxyInfoHolder
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

object Main extends FutureUtils {

  private val log = LoggerFactory.getLogger("com.ott.scheduler.Main")

  def createKalix(): Kalix = {
    // The KalixFactory automatically registers any generated Actions, Views or Entities,
    // and is kept up-to-date with any changes in your protobuf definitions.
    // If you prefer, you may remove this and manually register these components in a
    // `Kalix()` instance.
    KalixFactory.withComponents(
      new TaskTrackerEntity(_),
      new DoNothing1ServiceImpl(_),
      new TaskTrackerServiceImpl(_),
    )
  }

  def main(args: Array[String]): Unit = {
    log.info("Starting the Kalix service...")
    val kalixInst     = createKalix()
    val kalixRunner   = WrappedKalix(kalixInst).createRunner()
    val system        = kalixRunner.system
    val retrySettings = RetrySettings(3, 1.second, 3.seconds)

    import system.dispatcher

    def waitFuture(duration: FiniteDuration): Future[Unit] =
      Future(scala.concurrent.blocking(Thread.sleep(duration.toMillis)))

    def waitForProxyInfo: Future[Unit] = {
      val maxWaitMillis = 10000L
      val start         = System.currentTimeMillis()

      log.info("Waiting for Kalix proxy information...")

      def proxyInfoSet: Boolean = {
        val holder = ProxyInfoHolder(system)

        (holder.proxyPort.isDefined &&
        holder.proxyHostname.isDefined) ||
        holder.identificationInfo.isDefined
      }

      def waitLoop(): Future[Unit] =
        if (proxyInfoSet) {
          val temp = ProxyInfoHolder(system)
          println(s"Proxy host: ${temp.proxyHostname}")
          println(s"Proxy port: ${temp.proxyPort}")
          println(s"Identification Info: ${temp.identificationInfo}")
          Future.unit
        } else if ((System.currentTimeMillis() - start) > maxWaitMillis) {
          log.warn(
            s"No proxy information set after ${maxWaitMillis / 1000} seconds. The task scheduling call may fail."
          )
          Future.unit
        } else {
          waitFuture(100.millis).flatMap(_ => waitLoop())
        }

      waitLoop()
    }

    kalixRunner
      .run()
      .flatMap { _ =>
        def startClient1 = {
          val client1 = kalixRunner.grpcClients.getComponentGrpcClient(classOf[DoNothing1Service])
          backoffRetry(retrySettings)(client1.start(Empty.of()))
        }

        for {
          _    <- waitForProxyInfo
          _    <- Future.successful(log.info("Waiting to start scheduled tasks..."))
          _    <- waitFuture(10.seconds)
          _    <- Future.successful(log.info("Starting scheduled tasks..."))
          _    <- startClient1
          _    <- Future.successful(log.info("Tasks started."))
          done <- system.whenTerminated
        } yield done
      }
      .recoverWith { error =>
        log.error(s"Error during startup: ", error)
        kalixRunner.terminate
      }

  }

}
