package com.improving.utils

import akka.actor.{ActorSystem, BootstrapSetup}
import akka.stream.Materializer
import cats.effect.IO
import cats.effect.unsafe.{IORuntime, IORuntimeConfig}

import scala.concurrent._

/** A "god" type context which contains all of the asynchronous contexts for Scala, Akka, and Cats Effect. */
sealed abstract class AsyncContext {
  /** An [[ExecutionContext execution context]] which __should__ be used for blocking operations / IO. */
  def blockingContext: ExecutionContext

  /** Akka [[ActorSystem]] for Akka actors and related Akka stuff. */
  implicit def actorSystem: ActorSystem

  /** Akka [[Materializer materializer]] because Akka wants one. */
  implicit def materializer: Materializer

  /** Scala [[ExecutionContext execution context]] for [[Future Scala futures]]. */
  implicit def executionContext: ExecutionContext

  /** Cats effect [[IORuntime runtime]] for working with `IO`. */
  implicit def catsEffectRuntime: IORuntime

  /** Shuts down this [[AsyncContext]], synchronously (blocking operation), closing/releasing all resources. */
  def shutdown(): Unit

  /* Utility functions */

  /** Runs the provided expression (`bodyExpr`) on the [[blockingContext]]. */
  final def blockingCode[T](bodyExpr: => T): Future[T] = Future(bodyExpr)(blockingContext)

  /** Transforms the provided [[IO]] to one which when evaluated, will execute on the [[blockingContext]]. */
  final def blockingIO[T](io: IO[T]): IO[T] = io.evalOn(blockingContext)
}

object AsyncContext {
  final val defaultActorSystemName = "akka-async-context"
  final val defaultBootstrapSetup = BootstrapSetup()
  final val defaultIORuntimeConfig = IORuntimeConfig()

  def akka(name: String = defaultActorSystemName, setup: BootstrapSetup = defaultBootstrapSetup): AsyncContext =
    new ActorSystemContext(name, setup)

  def catsEffect(config: IORuntimeConfig = defaultIORuntimeConfig): AsyncContext =
    new CatsEffectAsyncContext(config)

  /** Akka [[ActorSystem]] driven [[AsyncContext]]. */
  final class ActorSystemContext private[AsyncContext] (name: String, setup: BootstrapSetup) extends AsyncContext {
    private[this] var _ioRuntime: Option[IORuntime] = None

    val (blockingContext, blockingContextShutdown) = IORuntime.createDefaultBlockingExecutionContext()

    implicit val actorSystem: ActorSystem = {
      val system = ActorSystem(name, setup)

      system.registerOnTermination(blockingContextShutdown())
      system.registerOnTermination(_ioRuntime.foreach(runtime => runtime.shutdown()))

      system
    }

    /** Cats effect [[IORuntime runtime]] for working with `IO`. */
    implicit def catsEffectRuntime: IORuntime =
      _ioRuntime match {
        case Some(runtime) => runtime
        case None => synchronized {
          _ioRuntime match {
            case Some(runtime) => runtime

            case None =>
              val runtime = IORuntime.builder()
                .setBlocking(blockingContext, () => ())
                .setCompute(actorSystem.dispatcher, () => ())
                .build()

              _ioRuntime = Some(runtime)
              runtime
          }
        }
      }

    /** Akka [[Materializer materializer]] because Akka wants one. */
    implicit lazy val materializer: Materializer = Materializer(actorSystem)

    /** Scala [[ExecutionContext execution context]] for [[Future Scala futures]]. */
    implicit def executionContext: ExecutionContext = actorSystem.dispatcher

    /** Shuts down this [[AsyncContext]], synchronously (blocking operation), closing/releasing all resources. */
    def shutdown(): Unit = {
      Await.result(actorSystem.terminate(), duration.FiniteDuration(10, "seconds"))
      ()
    }
  }

  /** Cats effect [[IORuntime]] driven [[AsyncContext]]. */
  final class CatsEffectAsyncContext private[AsyncContext] (config: IORuntimeConfig) extends AsyncContext {
    private[this] var _actorSystemInstance: Option[ActorSystem] = None

    val (blockingContext, blockingContextShutdown) = IORuntime.createDefaultBlockingExecutionContext()

    implicit val catsEffectRuntime: IORuntime = {
      val shutdownActorSystem = () => {
        _actorSystemInstance.fold(())(system =>
          Await.result(system.terminate(), duration.FiniteDuration(10, "seconds"))
        )
      }

      IORuntime.builder()
        .setConfig(config)
        .setBlocking(blockingContext, blockingContextShutdown)
        .addShutdownHook(shutdownActorSystem)
        .build()
    }

    /** Akka [[ActorSystem]] for Akka actors and related stuff. */
    implicit def actorSystem: ActorSystem =
      _actorSystemInstance match {
        case Some(system) => system

        case None => synchronized {
          _actorSystemInstance match {
            case Some(system) => system

            case None =>
              val system = ActorSystem("cats-effect-system", defaultExecutionContext = Some(catsEffectRuntime.compute))
              _actorSystemInstance = Some(system)
              system
          }
        }
      }

    /** Akka [[Materializer materializer]] because Akka wants one. */
    implicit lazy val materializer: Materializer = Materializer(actorSystem)

    /** Scala [[ExecutionContext execution context]] for [[Future Scala futures]]. */
    implicit def executionContext: ExecutionContext = catsEffectRuntime.compute

    /** Shuts down this [[AsyncContext]], synchronously (blocking operation), closing/releasing all resources. */
    def shutdown(): Unit = catsEffectRuntime.shutdown()
  }

}
