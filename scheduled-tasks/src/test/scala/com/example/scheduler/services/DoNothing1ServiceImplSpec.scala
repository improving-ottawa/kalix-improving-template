package com.example.scheduler.services

import com.example.scheduler.domain.TaskRunResult
import com.google.protobuf.empty.Empty
import kalix.scalasdk.action.Action
import kalix.scalasdk.testkit.ActionResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class DoNothing1ServiceImplSpec
    extends AnyWordSpec
    with Matchers {

  "DoNothing1ServiceImpl" must {

    "have example test that can be removed" in {
      val service = DoNothing1ServiceImplTestKit(new DoNothing1ServiceImpl(_))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // result.reply shouldBe expectedReply
    }

    "handle command Start" in {
      val service = DoNothing1ServiceImplTestKit(new DoNothing1ServiceImpl(_))
          pending
      // val result = service.start(Empty(...))
    }

    "handle command Run" in {
      val service = DoNothing1ServiceImplTestKit(new DoNothing1ServiceImpl(_))
          pending
      // val result = service.run(Empty(...))
    }

    "handle command RunForTest" in {
      val service = DoNothing1ServiceImplTestKit(new DoNothing1ServiceImpl(_))
          pending
      // val result = service.runForTest(Empty(...))
    }

  }
}
