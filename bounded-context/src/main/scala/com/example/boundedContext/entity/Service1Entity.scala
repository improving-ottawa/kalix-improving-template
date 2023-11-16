package com.example.boundedContext.entity

import com.example.boundedContext.domain.DoNothingCommand1
import com.example.boundedContext.domain.DoNothingResponse1
import com.example.boundedContext.domain.NoState1
import com.example.boundedContext.entity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Service1Entity(context: EventSourcedEntityContext) extends AbstractService1Entity {
  override def emptyState: NoState1 = NoState1.defaultInstance

  override def doNothing(
      currentState: NoState1,
      doNothingCommand: DoNothingCommand1
  ): EventSourcedEntity.Effect[DoNothingResponse1] = effects.reply(DoNothingResponse1.defaultInstance)

  override def doNothingResponse1(currentState: NoState1, doNothingResponse: DoNothingResponse1): NoState1 =
    currentState.update()

}
