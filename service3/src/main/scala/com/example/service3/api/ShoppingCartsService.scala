package com.example.service3.api

import com.example.common.domain.{CartLineItem, Product, ProductInCart}
import com.example.service3.domain._
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

  override def getCartDetails(request: GetShoppingCart): Action.Effect[CartDetails] = {
    @inline def createCartLineItem(product: Product, quantity: Int): CartLineItem =
      CartLineItem(
        ProductInCart(product.sku, product.name, product.price),
        quantity
      )

    effects.asyncReply(
      for {
        cart     <- components.cartService.getCart(request).execute()
        skus      = cart.items.view.map(_.productSku).toList
        products <- components.productsService.getProductsBySKU(MultipleProductsRequest(skus)).execute()
        prodMap   = products.products.view.map(p => (p.sku, p)).toMap
        prodItems = cart.items.view.map(li => createCartLineItem(prodMap(li.productSku), li.quantity)).toList
      } yield CartDetails(
        userId = cart.userId,
        cartItems = prodItems,
        cartTotal = prodItems.view.map(pli => pli.product.price * pli.quantity).reduce(_ + _)
      )
    )
  }

  override def createCart(createShoppingCart: CreateShoppingCart): Action.Effect[ShoppingCartId] =
    effects.asyncReply(
      components.cartService
        .createCart(createShoppingCart)
        .execute()
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
      components.cartService
        .addLineItem(request)
        .execute()
        .map(_ => renewAbandonedTimer(request.cartId))
    )

  override def removeItemFromCart(request: RemoveLineItem): Action.Effect[Empty] =
    effects.asyncReply(
      components.cartService
        .removeLineItem(request)
        .execute()
        .map(_ => renewAbandonedTimer(request.cartId))
    )

  override def startCartCheckout(request: StartCheckout): Action.Effect[CartDetails] =
    effects.asyncEffect(
      components.cartService
        .startCheckout(request)
        .execute()
        .map { _ =>
          val ignored = renewAbandonedTimer(request.cartId)
          getCartDetails(GetShoppingCart(request.cartId))
        }
    )

  override def completeCartCheckout(request: CompleteCheckout): Action.Effect[Empty] =
    effects.asyncReply(
      components.cartService
        .completeCheckout(request)
        .execute()
        .map { _ =>
          timers.cancel(s"${request.cartId}-cart")
          Empty.defaultInstance
        }
    )

}
