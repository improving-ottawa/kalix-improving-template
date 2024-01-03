package com.example.service3.api

import com.example.service3.domain.Order
import kalix.scalasdk.action.Action
import kalix.scalasdk.testkit.ActionResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class OrdersServiceSpec extends AnyWordSpec with Matchers {

  "OrdersService" must {

    "have example test that can be removed" in {
      val service = OrdersServiceTestKit(new OrdersService(_))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // result.reply shouldBe expectedReply
    }

    "handle command CreateOrderFromCart" in {
      val service = OrdersServiceTestKit(new OrdersService(_))
      pending
      // val result = service.createOrderFromCart(OrderFromCart(...))
    }

    "handle command GetOrderById" in {
      val service = OrdersServiceTestKit(new OrdersService(_))
      pending
      // val result = service.getOrderById(OrderById(...))
    }

  }
}
