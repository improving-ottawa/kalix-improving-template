package com.example.service3.domain

import com.example.service3.api._
import com.improving.utils.SystemClock

import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.

class CartService(context: EventSourcedEntityContext) extends AbstractCartService {
  override def emptyState: Cart = Cart.defaultInstance

  /* Command Handlers */

  override def createCart(currentState: Cart, req: CreateShoppingCart): EventSourcedEntity.Effect[CartCreated] = {
    val cartId = context.entityId

    val event = CartCreated(
      cartId,
      req.userId,
      whenCreated = SystemClock.currentInstant
    )

    effects.emitEvent(event).thenReply(_ => event)
  }

  override def addLineItem(currentState: Cart, req: AddLineItem): EventSourcedEntity.Effect[LineItemAdded] = {
    if (currentState.checkoutCompleted) effects.error("Cannot add items to a completed cart.")
    else {
      val event = LineItemAdded(currentState.userId, req.lineItem)
      effects.emitEvent(event).thenReply(_ => event)
    }
  }

  override def removeLineItem(currentState: Cart, req: RemoveLineItem): EventSourcedEntity.Effect[LineItemRemoved] = {
    if (currentState.checkoutCompleted) effects.error("Cannot remove items from a completed cart.")
    else {
      val event = LineItemRemoved(currentState.userId, req.lineItem)
      effects.emitEvent(event).thenReply(_ => event)
    }
  }

  override def getCart(currentState: Cart, req: GetShoppingCart): EventSourcedEntity.Effect[Cart] =
    effects.reply(currentState)

  override def abandonCart(currentState: Cart, req: AbandonShoppingCart): EventSourcedEntity.Effect[CartAbandoned] = {
    if (currentState.checkoutCompleted) effects.error("Cannot abandon a completed cart.")
    else {
      val event = CartAbandoned(currentState.userId, SystemClock.currentInstant)
      effects.emitEvent(event).thenReply(_ => event)
    }
  }

  override def startCheckout(currentState: Cart, req: StartCheckout): EventSourcedEntity.Effect[CheckoutStarted] = {
    if (currentState.checkoutCompleted) effects.error("Cannot start checkout for a completed cart.")
    else {
      val event = CheckoutStarted(currentState.userId, SystemClock.currentInstant)
      effects.emitEvent(event).thenReply(_ => event)
    }
  }

  override def completeCheckout(state: Cart, req: CompleteCheckout): EventSourcedEntity.Effect[CheckoutCompleted] = {
    if (state.checkoutCompleted) effects.error("Cannot complete checkout for a completed cart.")
    else {
      val event = CheckoutCompleted(state.userId, state.whenCheckoutStarted.getOrElse(SystemClock.currentInstant))

      // Don't delete the shopping cart (entity) because we want it later for metrics
      effects
        .emitEvent(event)
        .thenReply(_ => event)
    }
  }

  /* Event Handlers */

  override def cartCreated(currentState: Cart, event: CartCreated): Cart =
    currentState.copy(
      userId = event.userId,
      whenCreated = Some(event.whenCreated),
      items = List.empty
    )

  override def lineItemAdded(currentState: Cart, event: LineItemAdded): Cart =
    currentState.copy(items = currentState.items.appended(event.item))

  override def lineItemRemoved(currentState: Cart, event: LineItemRemoved): Cart = {
    val remItem = event.item
    currentState.copy(
      items =
        currentState.items.filterNot(li => li.productSku == remItem.productSku && li.quantity == remItem.quantity)
    )
  }

  override def cartAbandoned(currentState: Cart, cartAbandoned: CartAbandoned): Cart =
    currentState.copy(cartAbandoned = true)

  override def checkoutStarted(currentState: Cart, event: CheckoutStarted): Cart =
    currentState.copy(
      cartAbandoned = false,
      checkoutStarted = true,
      whenCheckoutStarted = Some(event.whenCheckoutStarted)
    )

  override def checkoutCompleted(currentState: Cart, checkoutCompleted: CheckoutCompleted): Cart =
    currentState.copy(
      cartAbandoned = false,
      checkoutCompleted = true
    )

}
