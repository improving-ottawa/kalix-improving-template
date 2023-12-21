package com.example.service3.api

import com.example.service3.domain.Cart
import com.google.protobuf.empty.Empty
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

import scala.concurrent.duration._

// This class was initially generated based on the .proto definition by Kalix tooling.

class ShoppingCartsService(creationContext: ActionCreationContext) extends AbstractShoppingCartsService {

  private def renewAbandonedTimer(cartId: String): Empty = {
    timers.cancel(s"$cartId-cart")
    timers.startSingleTimer(
      s"$cartId-cart",
      FiniteDuration(30, "minutes"),
      components.cartService.abandonCart(AbandonShoppingCart(cartId))
    )
    Empty.defaultInstance
  }

  override def getCart(getShoppingCart: GetShoppingCart): Action.Effect[Cart] =
    effects.forward(components.cartService.getCart(getShoppingCart))

  override def createCart(createShoppingCart: CreateShoppingCart): Action.Effect[ShoppingCartId] =
    effects.asyncReply(
      components.cartService.createCart(createShoppingCart).execute()
        .map { created =>
          // Shopping carts are automatically "abandoned" after 30 minutes"
          timers.startSingleTimer(
            s"${created.cartId}-cart",
            FiniteDuration(30, "minutes"),
            components.cartService.abandonCart(AbandonShoppingCart(created.cartId))
          )

          ShoppingCartId(created.cartId)
        }
    )

  override def addItemToCart(request: AddLineItem): Action.Effect[Empty] =
    effects.asyncReply(
      components.cartService.addLineItem(request).execute()
        .map(_ => renewAbandonedTimer(request.cartId))
    )

  override def removeItemFromCart(request: RemoveLineItem): Action.Effect[Empty] =
    effects.asyncReply(
      components.cartService.removeLineItem(request).execute()
        .map(_ => renewAbandonedTimer(request.cartId))
    )

  override def startCartCheckout(request: StartCheckout): Action.Effect[Empty] =
    effects.asyncReply(
      components.cartService.startCheckout(request).execute()
        .map(_ => renewAbandonedTimer(request.cartId))
    )

  override def completeCartCheckout(request: CompleteCheckout): Action.Effect[Empty] =
    effects.asyncReply(
      components.cartService.completeCheckout(request).execute()
        .map { _ =>
          timers.cancel(s"${request.cartId}-cart")
          Empty.defaultInstance
        }
    )

}
