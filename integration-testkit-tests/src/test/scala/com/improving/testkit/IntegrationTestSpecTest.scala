package com.improving.testkit

import com.example.gateway.api.Gateway

import com.google.protobuf.empty.Empty
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time._
import org.scalatest.wordspec.AnyWordSpecLike

/**
 * This spec tests that the [[IntegrationTestSpec]] spec itself is working properly.
 *
 * It also doubles as an example of how to use the [[IntegrationTestSpec]].
 */
class IntegrationTestSpecTest extends IntegrationTestSpec with AnyWordSpecLike with Matchers with ScalaFutures {

  // Patience configuration
  implicit private val patience: PatienceConfig = PatienceConfig(Span(5, Seconds), Span(500, Millis))

  // Setup the required Kalix services for this test spec.
  protected def configureTestKit(builder: TestKitBuilder): IntegrationTestKit =
    builder
      .withKalixService(KalixServiceManagerSpec.gatewayService)
      .withKalixService(KalixServiceManagerSpec.boundedContextService)
      .buildKit

  "IntegrationTestSpec" should {

    "allow for calling endpoints on running services" in {
      val client = testKit.getGrpcClient[Gateway]("gateway")

      val onlineCheckResult = client.onlineCheck(Empty.of()).futureValue
      onlineCheckResult mustNot be (null)

      val healthCheckIsHealthy = client.healthCheck(Empty.of()).map(_.isHealthy).futureValue
      healthCheckIsHealthy mustBe true
    }

  }

}
