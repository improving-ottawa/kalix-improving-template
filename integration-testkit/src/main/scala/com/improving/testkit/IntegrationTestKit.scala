package com.improving.testkit

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

/**
 * The full API for the [[IntegrationTestKit integration test kit]] available to you when using the
 * [[IntegrationTestSpec integration test spec]].
 *
 * @note No, you cannot extend or instantiate this trait from your code. Yes, that is intentional!
 */
trait IntegrationTestKit { self: internal.TestKitImpl =>

  def services: Iterable[KalixService]

  def hasRegisteredService(name: String): Boolean

  def getPortForService(serviceName: String): Option[Int]

  final def getGrpcClient[T : ClassTag](serviceName: String): T =
    getGrpcClient(implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]], serviceName)

  def getGrpcClient[T](clientClass: Class[T], serviceName: String): T

  def system: ActorSystem

  def materializer: Materializer

  implicit def executionContext: ExecutionContext

}
