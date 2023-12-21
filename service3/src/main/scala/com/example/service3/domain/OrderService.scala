package com.example.service3.domain

import com.improving.utils.SystemClock
import com.example.service3.api.OrderById

import kalix.scalasdk.valueentity.ValueEntity
import kalix.scalasdk.valueentity.ValueEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.

class OrderService(context: ValueEntityContext) extends AbstractOrderService {
  override def emptyState: Order = Order.defaultInstance

  override def createOrder(state: Order, request: CreateOrder): ValueEntity.Effect[Order] = {
    val newState = Order(
      orderId = context.entityId,
      userId = request.userId,
      cartId = request.cartId,
      orderNumber = int64ToOrderNumber(request.orderNumber),
      whenCreated = Some(SystemClock.currentInstant),
      shippingAddress = Some(request.shippingAddress),
      items = request.items,
      paymentInfo = Some(request.paymentInfo)
    )

    effects
      .updateState(newState)
      .thenReply(newState)
  }

  override def getOrder(state: Order, req: OrderById): ValueEntity.Effect[Order] =
    effects.reply(state)

  private def int64ToOrderNumber(value: Long): String = String.format("%08d", value)

}
