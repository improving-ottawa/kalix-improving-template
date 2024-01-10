package com.example.gateway.api

import com.example.gateway.domain.LoginData
import com.google.api.HttpBody
import kalix.scalasdk.action.Action
import kalix.scalasdk.testkit.ActionResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class AuthenticationServiceSpec
    extends AnyWordSpec
    with Matchers {

  "AuthenticationService" must {

    "have example test that can be removed" in {
      val service = AuthenticationServiceTestKit(new AuthenticationService(_))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // result.reply shouldBe expectedReply
    }

    "handle command oidcBeginLogin" in {
      val service = AuthenticationServiceTestKit(new AuthenticationService(_))
          pending
      // val result = service.oidcBeginLogin(BeginOIDCAuthenticationRequest(...))
    }

    "handle command oidcCompleteLogin" in {
      val service = AuthenticationServiceTestKit(new AuthenticationService(_))
          pending
      // val result = service.oidcCompleteLogin(CompleteOIDCLoginRequest(...))
    }

    "handle command passwordLogin" in {
      val service = AuthenticationServiceTestKit(new AuthenticationService(_))
          pending
      // val result = service.passwordLogin(PasswordAuthenticationRequest(...))
    }

  }
}
