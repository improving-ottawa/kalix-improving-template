package com.example.service3.domain

import com.example.service3.api.IssueNextOrderNumber
import com.example.service3.api.NextOrderNumber
import com.example.service3.domain
import kalix.scalasdk.testkit.ValueEntityResult
import kalix.scalasdk.valueentity.ValueEntity
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OrderNumberIssuerServiceSpec extends AnyWordSpec with Matchers {

  "OrderNumberIssuerService" must {

    "have example test that can be removed" in {
      val service = OrderNumberIssuerServiceTestKit(new OrderNumberIssuerService(_))
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

    "handle command IssueNextOrderNumber" in {
      val service = OrderNumberIssuerServiceTestKit(new OrderNumberIssuerService(_))
      pending
      // val result = service.issueNextOrderNumber(IssueNextOrderNumber(...))
    }

  }
}
