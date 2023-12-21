package com.example.service3.domain

import com.example.service3.api._

import kalix.scalasdk.valueentity.ValueEntity
import kalix.scalasdk.valueentity.ValueEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.

class OrderNumberIssuerService(context: ValueEntityContext) extends AbstractOrderNumberIssuerService {
  private final val maximumOrderNumber = 99999999L

  override def emptyState: OrderNumberIssuerState = OrderNumberIssuerState.defaultInstance

  override def issueNextOrderNumber(state: OrderNumberIssuerState, req: IssueNextOrderNumber): ValueEntity.Effect[NextOrderNumber] = {
    val nextOrderNumber = issueNextOrderNumber(state)
    val updatedState = OrderNumberIssuerState(nextOrderNumber)

    effects
      .updateState(updatedState)
      .thenReply(NextOrderNumber(nextOrderNumber))
  }

  private def issueNextOrderNumber(state: OrderNumberIssuerState): Long =
    if (state.lastOrderNumber == maximumOrderNumber) 1L
    else state.lastOrderNumber + 1L

}
