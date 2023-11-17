package com.example.service3.api

import com.example.service3.api
import com.example.service3.domain.DoNothingCommand3
import com.example.service3.domain.DoNothingResponse3
import com.example.service3.domain.NoState3
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Service3Service(context: EventSourcedEntityContext) extends AbstractService3Service {
  override def emptyState: NoState3 = NoState3.defaultInstance

  override def doNothing(
    currentState: NoState3,
    doNothingCommand3: DoNothingCommand3
  ): EventSourcedEntity.Effect[DoNothingResponse3] =
    effects.reply(DoNothingResponse3.defaultInstance)

  override def doNothingResponse3(currentState: NoState3, doNothingResponse3: DoNothingResponse3): NoState3 =
    currentState

}
