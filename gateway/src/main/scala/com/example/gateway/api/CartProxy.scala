package com.example.gateway.api

import com.example.gateway.middleware._
import com.example.service3.api._
import com.example.service3.domain._

import com.google.protobuf.empty.Empty
import kalix.scalasdk.action.Action

trait CartProxy extends GatewayProxyBase with AuthenticatedAction with AuthorizedAction {

  def createCart(createShoppingCart: CreateShoppingCart): Action.Effect[ShoppingCartId] =
    authenticatedEffect { _ =>
      effects.asyncReply(cartService.createCart(createShoppingCart))
    }

  def getCart(request: GetShoppingCart): Action.Effect[Cart] =
    authenticatedEffect { _ =>
      effects.asyncReply(cartService.getCart(request))
    }

  def addItemToCart(request: AddLineItem): Action.Effect[Empty] =
    authenticatedEffect { _ =>
      effects.asyncReply(cartService.addItemToCart(request))
    }

  def removeItemFromCart(request: RemoveLineItem): Action.Effect[Empty] =
    authenticatedEffect { _ =>
      effects.asyncReply(cartService.removeItemFromCart(request))
    }

  def startCartCheckout(request: StartCheckout): Action.Effect[CartDetails] =
    authenticatedEffect { _ =>
      effects.asyncReply(cartService.startCartCheckout(request))
    }

  def completeCartCheckout(request: CompleteCheckout): Action.Effect[Empty] =
    authenticatedEffect { _ =>
      effects.asyncReply(cartService.completeCartCheckout(request))
    }

}
