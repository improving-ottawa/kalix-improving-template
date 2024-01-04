package com.example.service3.domain

import com.example.service3.api.OrderById
import com.example.service3.domain
import kalix.scalasdk.testkit.ValueEntityResult
import kalix.scalasdk.valueentity.ValueEntity
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OrderServiceSpec extends AnyWordSpec with Matchers {

  "OrderService" must {

    "have example test that can be removed" in {
      val service = OrderServiceTestKit(new OrderService(_))
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

    "handle command CreateOrder" in {
      val service = OrderServiceTestKit(new OrderService(_))
      pending
      // val result = service.createOrder(CreateOrder(...))
    }

    "handle command GetOrder" in {
      val service = OrderServiceTestKit(new OrderService(_))
      pending
      // val result = service.getOrder(OrderById(...))
    }

  }
}
