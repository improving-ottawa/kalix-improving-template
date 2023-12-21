package com.example.service3.api

import com.google.protobuf.empty.Empty
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

// This class was initially generated based on the .proto definition by Kalix tooling.

private object OrderNumbersService {
  final val globalServiceId = "79ee5cd4-a044-11ee-8177-e7a043edcc6d"
}

class OrderNumbersService(creationContext: ActionCreationContext) extends AbstractOrderNumbersService {
  import OrderNumbersService.globalServiceId

  private val nextOrderNumberReq = IssueNextOrderNumber(globalServiceId)

  override def getNextOrderNumber(empty: Empty): Action.Effect[NextOrderNumber] =
    effects.forward(components.orderNumberIssuerService.issueNextOrderNumber(nextOrderNumberReq))

}
