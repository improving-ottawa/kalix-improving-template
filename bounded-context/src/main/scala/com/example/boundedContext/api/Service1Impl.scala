package com.example.boundedContext.api

import com.example.boundedContext.domain.DoNothingCommand1
import com.example.boundedContext.domain.DoNothingResponse1
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Service1Impl(creationContext: ActionCreationContext) extends AbstractService1Impl {

  override def doNothing(doNothingCommand: DoNothingCommand1): Action.Effect[DoNothingResponse1] = {
    throw new RuntimeException("The command handler for `DoNothing` is not implemented, yet")
  }
}
