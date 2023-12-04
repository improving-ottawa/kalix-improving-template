package com.example.gateway.api

import com.example.gateway.api
import com.example.gateway.domain.ClaimTokenRequest
import com.example.gateway.domain.ClaimTokenResponse
import com.example.gateway.domain.CreateLoginTokenRequest
import com.example.gateway.domain.CreateLoginTokenResponse
import com.example.gateway.domain.LoginTokenState
import com.improving.iam._
import com.google.protobuf.empty.Empty
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LoginTokenServiceSpec extends AnyWordSpec with Matchers {

  lazy val algorithmWithKeys: AlgorithmWithKeys = NoKeysPair // ???

  "LoginTokenService" must {

    "have example test that can be removed" in {
      val service = LoginTokenServiceTestKit(new LoginTokenService(_, algorithmWithKeys))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // val reply = result.getReply()
      // reply shouldBe expectedReply
      // verify the final state after the command
      // service.currentState() shouldBe expectedState
    }

    "handle command CreateLoginToken" in {
      val service = LoginTokenServiceTestKit(new LoginTokenService(_, algorithmWithKeys))
      pending
      // val result = service.createLoginToken(CreateLoginTokenRequest(...))
    }

    "handle command ClaimLoginToken" in {
      val service = LoginTokenServiceTestKit(new LoginTokenService(_, algorithmWithKeys))
      pending
      // val result = service.claimLoginToken(ClaimTokenRequest(...))
    }

    "handle command InvalidateLoginToken" in {
      val service = LoginTokenServiceTestKit(new LoginTokenService(_, algorithmWithKeys))
      pending
      // val result = service.invalidateLoginToken(ClaimTokenRequest(...))
    }

  }
}
