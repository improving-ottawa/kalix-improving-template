package com.example.service3.api

import com.example.service3.domain._

import com.google.protobuf.empty.Empty
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

// This class was initially generated based on the .proto definition by Kalix tooling.

class OrdersService(creationContext: ActionCreationContext) extends AbstractOrdersService {

  override def createOrderFromCart(req: OrderFromCart): Action.Effect[OrderConfirmation] =
    effects.asyncEffect {
      def createOrderAsync(cart: Cart, orderNumber: Long) = {
        val command = CreateOrder(req.userId, req.cartId, orderNumber, req.shippingAddress, cart.items, req.paymentInfo)
        components.orderService.createOrder(command).execute()
      }

      for {
        nextOrdNumber <- components.orderNumbersService.getNextOrderNumber(Empty.defaultInstance).execute()
        shoppingCart  <- components.cartService.getCart(GetShoppingCart(req.cartId)).execute()
        orderNum       = nextOrdNumber.orderNumber
        order         <- createOrderAsync(shoppingCart, orderNum)
        _             <- components.cartService.completeCheckout(CompleteCheckout(req.cartId)).execute()
      } yield {
        effects.reply(
          OrderConfirmation(
            orderId = order.orderId,
            userId = order.userId,
            orderNumber = order.orderNumber
          )
        )
      }
    }

  override def getOrderById(orderById: OrderById): Action.Effect[Order] =
    effects.forward(components.orderService.getOrder(orderById))

}
