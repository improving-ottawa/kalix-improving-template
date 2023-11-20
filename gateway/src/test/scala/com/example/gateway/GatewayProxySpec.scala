package com.example.gateway

import com.example.gateway.api.{GatewayProxy, GatewayProxyTestKit}
import com.example.gateway.domain.ClaimTokenRequest
import com.example.gateway.domain.ClaimTokenResponse
import com.example.gateway.domain.DoNothingTwiceCommand
import com.example.gateway.domain.DoNothingTwiceResponse
import com.example.gateway.domain.JwtValidationRequest
import com.example.gateway.domain.JwtValidationResponse
import com.example.gateway.domain.SendLoginLinkRequest
import com.example.gateway.domain.SendLoginLinkResponse
import com.google.protobuf.empty.Empty
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class GatewayProxySpec extends AnyWordSpec with Matchers {

  "GatewayProxy" must {

    "have example test that can be removed" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // result.reply shouldBe expectedReply
    }

    "handle command OnlineCheck" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_))
      pending
      // val result = service.onlineCheck(Empty(...))
    }

    "handle command HealthCheck" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_))
      pending
      // val result = service.healthCheck(Empty(...))
    }

    "handle command SendAdminLoginLink" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_))
      pending
      // val result = service.sendAdminLoginLink(SendLoginLinkRequest(...))
    }

    "handle command SendUserLoginLink" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_))
      pending
      // val result = service.sendUserLoginLink(SendLoginLinkRequest(...))
    }

    "handle command ClaimLoginToken" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_))
      pending
      // val result = service.claimLoginToken(ClaimTokenRequest(...))
    }

    "handle command ValidateJwt" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_))
      pending
      // val result = service.validateJwt(JwtValidationRequest(...))
    }

    "handle command DoNothingTwice" in {
      val service = GatewayProxyTestKit(new GatewayProxy(_))
      pending
      // val result = service.doNothingTwice(DoNothingTwiceCommand(...))
    }

  }
}
