package com.example.boundedContext.api

import com.example.common.PingThroughRequest
import com.example.common.PingThroughResponse
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class PingPong(creationContext: ActionCreationContext) extends AbstractPingPong {

  override def pingThrough(pingThroughRequest: PingThroughRequest): Action.Effect[PingThroughResponse] = {
    throw new RuntimeException("The command handler for `PingThrough` is not implemented, yet")
  }
}

