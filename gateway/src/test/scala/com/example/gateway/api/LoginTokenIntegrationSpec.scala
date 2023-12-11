package com.example.gateway.api

import com.example.gateway.Main
import com.example.gateway.domain._
import com.example.gateway.utils.JwtIssuerConfig
import com.improving.extensions.oidc.OIDCIdentityServiceConfig
import com.improving.iam.KeyLoaderConfig
import com.improving.utils.AsyncContext

import kalix.scalasdk.testkit.KalixTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.Millis
import org.scalatest.time.Seconds
import org.scalatest.time.Span
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LoginTokenIntegrationSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  implicit private val patience: PatienceConfig =
    PatienceConfig(Span(5, Seconds), Span(500, Millis))

  implicit private val asyncContext: AsyncContext = AsyncContext.akka("test-system")

  private val kalix = {
    val keyLoaderConfig = KeyLoaderConfig(
      pdi.jwt.JwtAlgorithm.RS256,
      "does_not_exist.pub",
      "does_not_exist.priv",
      None
    )

    val identityConfig = OIDCIdentityServiceConfig(
      providerCallback = sttp.model.Uri.unsafeParse("http://localhost:9000/oidc/callback"),
      Map.empty
    )

    val jwtIssuerConfig = JwtIssuerConfig(
      tokenIssuerUrl = "http://localhost:9000",
      tokenValidDuration = scala.concurrent.duration.FiniteDuration(5, "minutes"),
      defaultUserRole = "Test"
    )

    Main.createKalix(keyLoaderConfig, identityConfig, jwtIssuerConfig)
  }

  private val testKit = KalixTestKit(kalix).start()

  private val client = testKit.getGrpcClient(classOf[LoginToken])

  "LoginToken" must {

    "have example test that can be removed" in {
      pending
      // use the gRPC client to send requests to the
      // proxy and verify the results
    }

  }

  override def afterAll(): Unit = {
    testKit.stop()
    super.afterAll()
  }

}
