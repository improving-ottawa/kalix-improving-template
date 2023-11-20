package com.example.service3.api

import com.example.common._
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class PingPong(creationContext: ActionCreationContext) extends AbstractPingPong {

  override def pingThrough(req: PingThroughRequest): Action.Effect[PingThroughResponse] =
    effects.reply(PingThroughHandler.response(req, "service3.service3"))

}
