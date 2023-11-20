package com.example.service3.api

import com.example.service3.domain.DoNothingCommand3
import com.google.protobuf.any.{Any => ScalaPbAny}
import com.google.protobuf.empty.Empty
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class NoData3Service(context: ViewContext) extends AbstractNoData3Service {

  override def emptyState: Empty = Empty.defaultInstance

  override def processDoNothing3(state: Empty, doNothingCommand3: DoNothingCommand3): UpdateEffect[Empty] =
    effects.updateState(Empty.defaultInstance)

}
