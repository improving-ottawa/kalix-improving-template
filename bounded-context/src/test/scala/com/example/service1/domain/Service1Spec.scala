package com.example.service1.domain

import kalix.scalasdk.testkit.ValueEntityResult
import kalix.scalasdk.valueentity.ValueEntity
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class Service1Spec extends AnyWordSpec with Matchers {

  "Service1" must {

    "have example test that can be removed" in {
      val service = Service1TestKit(new Service1(_))
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
      val service = Service1TestKit(new Service1(_))
      pending
      // val result = service.doNothing(DoNothingCommand(...))
    }

  }
}
