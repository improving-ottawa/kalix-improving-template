package com.improving.template.domain

import com.google.protobuf.empty.Empty
import com.improving.template
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Service1(context: EventSourcedEntityContext) extends AbstractService1 {
  override def emptyState: NoState =
    NoState()

  override def doNothing(
      currentState: NoState,
      doNothingCommand: DoNothingCommand
  ): EventSourcedEntity.Effect[Empty] = {
    effects.emitEvent(DoNothingEvent)
    effects.reply(Empty())
  }

  override def doNothingEvent(currentState: NoState, doNothingEvent: DoNothingEvent): NoState =
    currentState
}
