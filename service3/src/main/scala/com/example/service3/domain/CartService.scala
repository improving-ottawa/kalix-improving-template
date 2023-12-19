package com.example.service3.domain

import com.example.service3.api.AbandonShoppingCart
import com.example.service3.api.AddLineItem
import com.example.service3.api.CheckoutShoppingCart
import com.example.service3.api.CreateShoppingCart
import com.example.service3.api.GetShoppingCart
import com.example.service3.api.RemoveLineItem
import com.example.service3.domain
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class CartService(context: EventSourcedEntityContext) extends AbstractCartService {
  override def emptyState: Cart =
    throw new UnsupportedOperationException("Not implemented yet, replace with your empty entity state")

  override def createCart(currentState: Cart, createShoppingCart: CreateShoppingCart): EventSourcedEntity.Effect[CartCreated] =
    effects.error("The command handler for `CreateCart` is not implemented, yet")

  override def addLineItem(currentState: Cart, addLineItem: AddLineItem): EventSourcedEntity.Effect[LineItemAdded] =
    effects.error("The command handler for `AddLineItem` is not implemented, yet")

  override def removeLineItem(currentState: Cart, removeLineItem: RemoveLineItem): EventSourcedEntity.Effect[LineItemRemoved] =
    effects.error("The command handler for `RemoveLineItem` is not implemented, yet")

  override def getCart(currentState: Cart, getShoppingCart: GetShoppingCart): EventSourcedEntity.Effect[Cart] =
    effects.error("The command handler for `GetCart` is not implemented, yet")

  override def abandonCart(currentState: Cart, abandonShoppingCart: AbandonShoppingCart): EventSourcedEntity.Effect[CartAbandoned] =
    effects.error("The command handler for `AbandonCart` is not implemented, yet")

  override def startCheckout(currentState: Cart, checkoutShoppingCart: CheckoutShoppingCart): EventSourcedEntity.Effect[CheckoutStarted] =
    effects.error("The command handler for `StartCheckout` is not implemented, yet")

  override def cartCreated(currentState: Cart, cartCreated: CartCreated): Cart =
    throw new RuntimeException("The event handler for `CartCreated` is not implemented, yet")

  override def lineItemAdded(currentState: Cart, lineItemAdded: LineItemAdded): Cart =
    throw new RuntimeException("The event handler for `LineItemAdded` is not implemented, yet")

  override def lineItemRemoved(currentState: Cart, lineItemRemoved: LineItemRemoved): Cart =
    throw new RuntimeException("The event handler for `LineItemRemoved` is not implemented, yet")

  override def cartAbandoned(currentState: Cart, cartAbandoned: CartAbandoned): Cart =
    throw new RuntimeException("The event handler for `CartAbandoned` is not implemented, yet")

  override def checkoutStarted(currentState: Cart, checkoutStarted: CheckoutStarted): Cart =
    throw new RuntimeException("The event handler for `CheckoutStarted` is not implemented, yet")

}
