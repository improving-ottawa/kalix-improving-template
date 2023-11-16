package com.example.boundedContext.api

import com.example.common.{PingThroughHandler, PingThroughRequest, PingThroughResponse, ServiceTrace}
import com.example.utils.SystemClock
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

/** Shared handler for [[PingThroughRequest]] handling. */

class PingPong(creationContext: ActionCreationContext) extends AbstractPingPong {

  /** Creates a [[PingThroughResponse]], given a [[PingThroughRequest request]] and a `serviceName`. */

  override def pingThrough(pingThroughRequest: PingThroughRequest): Action.Effect[PingThroughResponse] =
    effects.reply(PingThroughHandler.response(pingThroughRequest, "boundedContext.boundedContext"))

}
