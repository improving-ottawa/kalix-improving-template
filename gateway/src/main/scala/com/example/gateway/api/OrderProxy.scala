package com.example.gateway.api

import com.example.gateway.middleware._
import com.example.service3.api._
import com.example.service3.domain._

import kalix.scalasdk.action.Action

trait OrderProxy extends GatewayProxyBase with AuthenticatedAction with AuthorizedAction {

  def createOrderFromCart(request: OrderFromCart): Action.Effect[OrderConfirmation] =
    authenticatedEffect(_ =>
      effects.asyncReply(ordersService.createOrderFromCart(request))
    )

  def getOrderById(request: OrderById): Action.Effect[Order] =
    authenticatedEffect(_ =>
      effects.asyncReply(ordersService.getOrderById(request))
    )

}
