package com.example.service3.api

import com.example.service3.entity.{Service3Entity, Service3EntityTestKit}
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.testkit.EventSourcedResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Service3EntitySpec extends AnyWordSpec with Matchers {
  "The Service3Entity" should {
    "have example test that can be removed" in {
      val testKit = Service3EntityTestKit(new Service3Entity(_))
      pending
      // use the testkit to execute a command:
      // val result: EventSourcedResult[R] = testKit.someOperation(SomeRequest("id"));
      // verify the emitted events
      // val actualEvent: ExpectedEvent = result.nextEventOfType[ExpectedEvent]
      // actualEvent shouldBe expectedEvent
      // verify the final state after applying the events
      // testKit.state() shouldBe expectedState
      // verify the reply
      // result.reply shouldBe expectedReply
      // verify the final state after the command
    }

    "correctly process commands of type DoNothing3" in {
      val testKit = Service3EntityTestKit(new Service3Entity(_))
      pending
      // val result: EventSourcedResult[CompanyCreated] = testKit.doCreateCompany(CreateCompany(...))
    }
  }
}
