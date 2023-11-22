package com.improving.testkit

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

trait IntegrationTestKitApi {

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
