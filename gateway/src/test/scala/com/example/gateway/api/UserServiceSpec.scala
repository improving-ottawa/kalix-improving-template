package com.example.gateway.api

import com.example.gateway.domain.LocalIdentityRegistration
import kalix.scalasdk.action.Action
import kalix.scalasdk.testkit.ActionResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class UserServiceSpec extends AnyWordSpec with Matchers {

  "UserService" must {

    "have example test that can be removed" in {
      val service = UserServiceTestKit(new UserService(_))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // result.reply shouldBe expectedReply
    }

    "handle command GetUserLoginInfo" in {
      val service = UserServiceTestKit(new UserService(_))
      pending
      // val result = service.getUserLoginInfo(UserEmailRequest(...))
    }

    "handle command RegisterNewLocalUser" in {
      val service = UserServiceTestKit(new UserService(_))
      pending
      // val result = service.registerNewLocalUser(LocalIdentityRegistration(...))
    }

    "handle command GetUserIdBySubject" in {
      val service = UserServiceTestKit(new UserService(_))
      pending
      // val result = service.getUserIdBySubject(ForeignIdentityRequest(...))
    }

    "handle command GetUserInfo" in {
      val service = UserServiceTestKit(new UserService(_))
      pending
      // val result = service.getUserInfo(GetUserRequest(...))
    }

    "handle command UpdateLocalUserIdentity" in {
      val service = UserServiceTestKit(new UserService(_))
      pending
      // val result = service.updateLocalUserIdentity(UpdateLocalIdentityRequest(...))
    }

  }
}
