package com.example.service3.entity

import com.example.service3.api
import com.example.service3.domain.DoNothingCommand3
import com.example.service3.domain.DoNothingResponse3
import com.example.service3.domain.NoState3
import kalix.scalasdk.valueentity.ValueEntity
import kalix.scalasdk.valueentity.ValueEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Service3Entity(context: ValueEntityContext) extends AbstractService3Entity {
  override def emptyState: NoState3 = NoState3.defaultInstance

  override def doNothing(
    currentState: NoState3,
    doNothingCommand3: DoNothingCommand3
  ): ValueEntity.Effect[DoNothingResponse3] =
    effects.updateState(currentState).thenReply(DoNothingResponse3.defaultInstance)

}
