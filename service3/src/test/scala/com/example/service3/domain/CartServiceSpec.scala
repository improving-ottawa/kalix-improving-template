package com.example.service3.domain

import com.example.service3.api.AbandonShoppingCart
import com.example.service3.api.AddLineItem
import com.example.service3.api.CheckoutShoppingCart
import com.example.service3.api.CreateShoppingCart
import com.example.service3.api.GetShoppingCart
import com.example.service3.api.RemoveLineItem
import com.example.service3.domain
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.testkit.EventSourcedResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class CartServiceSpec extends AnyWordSpec with Matchers {
  "The CartService" should {
    "have example test that can be removed" in {
      val testKit = CartServiceTestKit(new CartService(_))
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

    "correctly process commands of type CreateCart" in {
      val testKit = CartServiceTestKit(new CartService(_))
      pending
      // val result: EventSourcedResult[CartCreated] = testKit.createCart(CreateShoppingCart(...))
    }

    "correctly process commands of type AddLineItem" in {
      val testKit = CartServiceTestKit(new CartService(_))
      pending
      // val result: EventSourcedResult[LineItemAdded] = testKit.addLineItem(AddLineItem(...))
    }

    "correctly process commands of type RemoveLineItem" in {
      val testKit = CartServiceTestKit(new CartService(_))
      pending
      // val result: EventSourcedResult[LineItemRemoved] = testKit.removeLineItem(RemoveLineItem(...))
    }

    "correctly process commands of type GetCart" in {
      val testKit = CartServiceTestKit(new CartService(_))
      pending
      // val result: EventSourcedResult[Cart] = testKit.getCart(GetShoppingCart(...))
    }

    "correctly process commands of type AbandonCart" in {
      val testKit = CartServiceTestKit(new CartService(_))
      pending
      // val result: EventSourcedResult[CartAbandoned] = testKit.abandonCart(AbandonShoppingCart(...))
    }

    "correctly process commands of type StartCheckout" in {
      val testKit = CartServiceTestKit(new CartService(_))
      pending
      // val result: EventSourcedResult[CheckoutStarted] = testKit.startCheckout(CheckoutShoppingCart(...))
    }
  }
}
