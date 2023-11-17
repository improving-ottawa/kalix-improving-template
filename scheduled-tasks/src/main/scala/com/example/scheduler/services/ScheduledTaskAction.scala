package com.example.scheduler.services

import com.example.scheduler._
import com.example.scheduler.api._
import com.example.scheduler.domain._
import com.example.scheduler.entity._
import com.example.utils._
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.protobuf.empty.Empty
import kalix.javasdk.impl.GrpcClients
import kalix.scalasdk.DeferredCall
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent._
import scala.concurrent.duration._
import java.time._

trait ScheduledTaskAction extends Action with FutureUtils { self =>
  val taskKey: String

  protected def creationContext: ActionCreationContext
  protected def components: Components

  protected def retrySettings: RetrySettings = RetrySettings(20, 500.millis, 30.seconds)

  final protected val logger: Logger = LoggerFactory.getLogger(self.getClass.getName.stripSuffix("$"))

  final protected val runningIntegrationTests = {
    val sysProp = Option(System.getProperty("integration.test"))
    sysProp.exists(_.nonEmpty)
  }

  implicit final protected lazy val materializer: Materializer = creationContext.materializer
  final protected lazy val system: ActorSystem                 = materializer.system
  final protected lazy val grpcClients: GrpcClients            = kalix.javasdk.impl.GrpcClients.get(system)

  // Must implement

  protected def calculateDelayUntilNextRunTime(now: Instant): FiniteDuration

  protected def taskAction(nowInstant: Instant): Future[Unit]

  protected def runTaskDeferredCall: DeferredCall[Empty, Empty]

  // Utility functions

  final protected def retryFuture[A](future: => Future[A]): Future[A] = backoffRetry(retrySettings)(future)

  // Kalix Action implementation

  final def start(empty: Empty): Action.Effect[Empty] = {
    val checkCanBeScheduled: Future[Boolean] =
      components.taskTrackerEntity.getTaskStatus(TaskStatusRequest(taskKey)).execute().map {
        case TaskStatus(_, TaskState.TASK_STATE_RUNNING, _)   => false
        case TaskStatus(_, TaskState.TASK_STATE_SCHEDULED, _) => false
        case _                                                => true
      }

    val rescheduleFuture =
      checkCanBeScheduled.flatMap { canSchedule =>
        if (!canSchedule) Future.successful(Empty.of())
        else
          for {
            _ <- cancelExistingTimer()
            _ <- scheduleNextRun()
          } yield Empty.of()
      }

    effects.asyncReply(rescheduleFuture)
  }

  final def run(empty: Empty): Action.Effect[Empty] = {
    def attemptStartRun: Future[Boolean] =
      components.taskTrackerEntity.markTaskRunning(TaskCommand(taskKey)).execute().map(_.canStart).map {
        case true  => logger.info("Starting task..."); true
        case false => logger.warn("Task is already running."); false
      }

    def runTaskAction: Future[Unit] = {
      val startTime = SystemClock.currentInstant

      val taskFuture: Future[Unit] =
        for {
          _ <- cancelExistingTimer()
          _ <- taskAction(startTime)
          _ <- markTaskComplete(startTime)
          _ <- scheduleNextRun()
          _ <- Future.successful(logger.info(s"Task completed successfully."))
        } yield ()

      taskFuture.recoverWith { error =>
        logger.error("Task failed due to an unexpected error:", error)
        for {
          _ <- markTaskFailed(startTime, error)
          _ <- scheduleNextRun()
        } yield ()
      }
    }

    effects.asyncReply(
      attemptStartRun.flatMap {
        case false => Future.successful(Empty.of)
        case true  => fireAndForgetFuture(runTaskAction).map(_ => Empty.of())
      }
    )
  }

  final def runForTest(empty: Empty): Action.Effect[TaskRunResult] = {
    def runTaskAction: Future[TaskRunResult] = {
      val startTime = SystemClock.currentInstant

      val taskFuture: Future[TaskRunResult] =
        for {
          _      <- taskAction(startTime)
          result <- markTaskComplete(startTime)
        } yield result

      taskFuture.recoverWith { error =>
        logger.error("Task failed due to an unexpected error:", error)
        markTaskFailed(startTime, error)
      }
    }

    effects.asyncReply(runTaskAction)
  }

  // Scheduled Task implementation

  final private lazy val timerKey: String = s"${taskKey}Timer"

  private def cancelExistingTimer(): Future[Unit] = timers.cancel(timerKey).map(_ => ()).recover(_ => ())

  private def scheduleNextRun(): Future[Unit] = {
    val now       = SystemClock.currentInstant
    val delay     = calculateDelayUntilNextRunTime(now)
    val delayText = s"${delay.toHours}h ${delay.toMinutes % 60L}m"
    logger.info(s"Scheduling next execution in: $delayText")

    for {
      _ <- components.taskTrackerEntity.markTaskScheduled(TaskCommand(taskKey)).execute()
      _ <- timers.startSingleTimer(name = timerKey, delay = delay, deferredCall = runTaskDeferredCall)
    } yield ()
  }

  private def fireAndForgetFuture(future: => Future[_]): Future[Unit] = {
    val dispatched = Promise[Unit]()

    Future.unit.flatMap { _ =>
      dispatched.success(())
      future
    }

    dispatched.future
  }

  private def markTaskComplete(startTime: Instant): Future[TaskRunResult] = {
    val runSuccess = TaskRunSuccess(startTime, SystemClock.currentInstant)
    val result     = TaskRunResult(TaskRunResult.Value.Success(runSuccess))

    components.taskTrackerEntity
      .markTaskCompleted(TaskCompletedCommand(taskKey, result))
      .execute()
      .map(_ => result)
  }

  private def markTaskFailed(startTime: Instant, error: Throwable): Future[TaskRunResult] = {
    val runFailure = TaskRunFailure(startTime, SystemClock.currentInstant, error.getMessage)
    val result     = TaskRunResult(TaskRunResult.Value.Failure(runFailure))

    components.taskTrackerEntity
      .markTaskCompleted(TaskCompletedCommand(taskKey, result))
      .execute()
      .map(_ => result)
  }

}
