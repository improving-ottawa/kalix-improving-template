package com.example.service3.entity

import com.example.service3.api
import com.example.service3.domain.DoNothingCommand3
import com.example.service3.domain.DoNothingResponse3
import com.example.service3.domain.NoState3
import kalix.scalasdk.testkit.ValueEntityResult
import kalix.scalasdk.valueentity.ValueEntity
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class Service3EntitySpec extends AnyWordSpec with Matchers {

  "Service3Entity" must {

    "have example test that can be removed" in {
      val service = Service3EntityTestKit(new Service3Entity(_))
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

    "handle command DoNothing" in {
      val service = Service3EntityTestKit(new Service3Entity(_))
      pending
      // val result = service.doNothing(DoNothingCommand3(...))
    }

  }
}
