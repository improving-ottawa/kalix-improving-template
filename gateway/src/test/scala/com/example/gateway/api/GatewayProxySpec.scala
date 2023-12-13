package com.example.gateway.api

import com.example.gateway.utils.{JwtIssuer, JwtIssuerConfig}
import com.improving.iam.{AlgorithmWithKeys, NoKeysPair}

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.FiniteDuration

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class GatewayProxySpec extends AnyWordSpec with Matchers {

  private val algorithmWithKeys: AlgorithmWithKeys = NoKeysPair

  private val blankJwtIssuerConfig = JwtIssuerConfig(
    tokenIssuerUrl = "http://localhost:9000",
    tokenValidDuration = FiniteDuration(1, "second"),
    defaultUserRole = "None"
  )

  private val jwtIssuer = JwtIssuer(blankJwtIssuerConfig, algorithmWithKeys)

  "GatewayProxy" must {

    "have example test that can be removed" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_, jwtIssuer))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // result.reply shouldBe expectedReply
    }

    "handle command OnlineCheck" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_, jwtIssuer))
      pending
      // val result = service.onlineCheck(Empty(...))
    }

    "handle command HealthCheck" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_, jwtIssuer))
      pending
      // val result = service.healthCheck(Empty(...))
    }

    "handle command SendAdminLoginLink" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_, jwtIssuer))
      pending
      // val result = service.sendAdminLoginLink(SendLoginLinkRequest(...))
    }

    "handle command SendUserLoginLink" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_, jwtIssuer))
      pending
      // val result = service.sendUserLoginLink(SendLoginLinkRequest(...))
    }

    "handle command ClaimLoginToken" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_, jwtIssuer))
      pending
      // val result = service.claimLoginToken(ClaimTokenRequest(...))
    }

    "handle command ValidateJwt" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_, jwtIssuer))
      pending
      // val result = service.validateJwt(JwtValidationRequest(...))
    }

    "handle command DoNothingTwice" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_, jwtIssuer))
      pending
      // val result = service.doNothingTwice(DoNothingTwiceCommand(...))
    }

  }
}
