package com.example.utils

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Try

trait FutureUtils {
  import FutureUtils.{calculateDelay, defaultOnFailure}

  final protected type RetrySettings = FutureUtils.RetrySettings
  final protected val RetrySettings = FutureUtils.RetrySettings

  final protected def backoffRetry[A](settings: RetrySettings, onFailure: Throwable => Unit = defaultOnFailure)(futureSrc: => Future[A])(implicit
    ec: ExecutionContext
  ): Future[A] = retryLoop(settings, () => futureSrc, onFailure)

  final protected def futureSleep(duration: FiniteDuration)(implicit ec: ExecutionContext): Future[Unit] = {
    val totalMillis = duration.toMillis
    if (totalMillis <= 0L) Future.unit
    else Future(blocking(Thread.sleep(totalMillis)))
  }

  final private[this] def retryLoop[A](settings: RetrySettings, futureSrc: () => Future[A], onFailure: Throwable => Unit, failures: Int = 0)(implicit
    ec: ExecutionContext
  ): Future[A] =
    futureSrc().recoverWith { error =>
      if (failures == settings.maxRetries) Future.failed(error)
      else {
        val restarts     = failures + 1
        val nextDelay    = calculateDelay(restarts, settings.minBackoff, settings.maxBackoff, settings.randomFactor)
        val tryOnFailure = Try(onFailure(error))
        for {
          _   <- Future.fromTry(tryOnFailure)
          _   <- futureSleep(nextDelay)
          res <- retryLoop(settings, futureSrc, onFailure, restarts)
        } yield res
      }
    }

}

object FutureUtils {

  final private val defaultOnFailure: Throwable => Unit =
    _ => ()

  final case class RetrySettings(
    maxRetries: Int,
    minBackoff: FiniteDuration,
    maxBackoff: FiniteDuration,
    randomFactor: Double = 0.1
  )

  private def calculateDelay(
    restartCount: Int,
    minBackoff: FiniteDuration,
    maxBackoff: FiniteDuration,
    randomFactor: Double
  ): FiniteDuration = {
    val rnd                = 1.0 + java.util.concurrent.ThreadLocalRandom.current().nextDouble() * randomFactor
    val calculatedDuration = Try(maxBackoff.min(minBackoff * math.pow(2, restartCount)) * rnd).getOrElse(maxBackoff)
    calculatedDuration match {
      case f: FiniteDuration => f
      case _                 => maxBackoff
    }
  }

}
