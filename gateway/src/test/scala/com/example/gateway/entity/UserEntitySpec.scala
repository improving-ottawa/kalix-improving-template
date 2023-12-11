package com.example.gateway.entity

import com.example.gateway.domain.UserInfo
import com.example.gateway.entity
import com.google.protobuf.empty.Empty
import kalix.scalasdk.testkit.ValueEntityResult
import kalix.scalasdk.valueentity.ValueEntity
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UserEntitySpec
    extends AnyWordSpec
    with Matchers {

  "UserEntity" must {

    "have example test that can be removed" in {
      val service = UserEntityTestKit(new UserEntity(_))
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

    "handle command GetUser" in {
      val service = UserEntityTestKit(new UserEntity(_))
      pending
      // val result = service.getUser(GetUserRequest(...))
    }

    "handle command CreateOrUpdateUserInfo" in {
      val service = UserEntityTestKit(new UserEntity(_))
      pending
      // val result = service.createOrUpdateUserInfo(UserInfo(...))
    }

    "handle command UpdateUserRoles" in {
      val service = UserEntityTestKit(new UserEntity(_))
      pending
      // val result = service.updateUserRoles(UpdateUserRolesRequest(...))
    }

  }
}
