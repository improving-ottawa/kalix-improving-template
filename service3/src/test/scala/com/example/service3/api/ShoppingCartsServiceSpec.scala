package com.example.service3.api

import com.example.service3.domain.Cart
import com.example.service3.domain.CartDetails
import com.google.protobuf.empty.Empty
import kalix.scalasdk.action.Action
import kalix.scalasdk.testkit.ActionResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class ShoppingCartsServiceSpec extends AnyWordSpec with Matchers {

  "ShoppingCartsService" must {

    "have example test that can be removed" in {
      val service = ShoppingCartsServiceTestKit(new ShoppingCartsService(_))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // result.reply shouldBe expectedReply
    }

    "handle command GetCart" in {
      val service = ShoppingCartsServiceTestKit(new ShoppingCartsService(_))
      pending
      // val result = service.getCart(GetShoppingCart(...))
    }

    "handle command GetCartDetails" in {
      val service = ShoppingCartsServiceTestKit(new ShoppingCartsService(_))
      pending
      // val result = service.getCartDetails(GetShoppingCart(...))
    }

    "handle command CreateCart" in {
      val service = ShoppingCartsServiceTestKit(new ShoppingCartsService(_))
      pending
      // val result = service.createCart(CreateShoppingCart(...))
    }

    "handle command AddItemToCart" in {
      val service = ShoppingCartsServiceTestKit(new ShoppingCartsService(_))
      pending
      // val result = service.addItemToCart(AddLineItem(...))
    }

    "handle command RemoveItemFromCart" in {
      val service = ShoppingCartsServiceTestKit(new ShoppingCartsService(_))
      pending
      // val result = service.removeItemFromCart(RemoveLineItem(...))
    }

    "handle command StartCartCheckout" in {
      val service = ShoppingCartsServiceTestKit(new ShoppingCartsService(_))
      pending
      // val result = service.startCartCheckout(StartCheckout(...))
    }

    "handle command CompleteCartCheckout" in {
      val service = ShoppingCartsServiceTestKit(new ShoppingCartsService(_))
      pending
      // val result = service.completeCartCheckout(CompleteCheckout(...))
    }

  }
}
