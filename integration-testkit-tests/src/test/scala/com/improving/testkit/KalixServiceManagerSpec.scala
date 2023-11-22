package com.improving.testkit

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

object KalixServiceManagerSpec {

  val gatewayService: KalixService =
    KalixService(
      serviceName = "gateway",
      kalix = com.example.gateway.Main.createKalix()
    )

  val boundedContextService: KalixService =
    KalixService(
      serviceName = "bounded-context",
      kalix = com.example.boundedContext.Main.createKalix()
    )

  private def checkSocketConnection(port: Int) =
    try {
      val sock = new java.net.Socket("localhost", port)
      try sock.isConnected
      finally sock.close()
    } catch { case _: Throwable => false }

}

class KalixServiceManagerSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {
  import KalixServiceManagerSpec._

  val manager = new TestServiceManager

  override def afterAll(): Unit = {
    println("Stopping ServiceManager...")
    manager.stopAll()
  }

  override def beforeAll(): Unit = {
    println("Registering Kalix services with manager...")
    manager.addForTest(gatewayService)
    manager.addForTest(boundedContextService)
  }

  /* Test Spec(s) */
  "KalixServiceManager" should {

    "be able to start all services successfully" in {
      val expectedServiceNames = Seq(gatewayService.serviceName, boundedContextService.serviceName)
      manager.startAll()

      val runningServices = manager.runningServices
      val actualServiceNames = runningServices.map(_.serviceName)

      actualServiceNames must contain theSameElementsAs(expectedServiceNames)

      runningServices.map { service =>
        val jvmServicePort = service.jvmServicePort
        val hostProxyPort = service.kalixProxyPort

        jvmServicePort mustNot be (0)
        hostProxyPort mustNot be (0)

        checkSocketConnection(jvmServicePort) mustBe true
        checkSocketConnection(hostProxyPort) mustBe true
      }
    }

  }

}
