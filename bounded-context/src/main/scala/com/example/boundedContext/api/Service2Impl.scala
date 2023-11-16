package com.example.boundedContext.api

import com.example.boundedContext.domain.{DoNothingCommand2, DoNothingResponse2}
import kalix.scalasdk.action.{Action, ActionCreationContext}

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Service2Impl(creationContext: ActionCreationContext) extends AbstractService2Impl {

  override def doNothing(doNothingCommand: DoNothingCommand2): Action.Effect[DoNothingResponse2] =
    effects.reply(DoNothingResponse2.defaultInstance)
}
