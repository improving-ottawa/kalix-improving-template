package com.example.boundedContext.api

import com.example.boundedContext.domain.DoNothingCommand2
import com.example.boundedContext.domain.DoNothingResponse2
import kalix.scalasdk.action.Action
import kalix.scalasdk.testkit.ActionResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Service2ImplSpec
    extends AnyWordSpec
    with Matchers {

  "Service2Impl" must {

    "have example test that can be removed" in {
      val service = Service2ImplTestKit(new Service2Impl(_))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // result.reply shouldBe expectedReply
    }

    "handle command DoNothing" in {
      val service = Service2ImplTestKit(new Service2Impl(_))
          pending
      // val result = service.doNothing(DoNothingCommand2(...))
    }

  }
}
