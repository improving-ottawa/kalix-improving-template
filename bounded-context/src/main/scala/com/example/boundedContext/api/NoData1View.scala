package com.example.boundedContext.api

import com.google.protobuf.empty.Empty
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class NoData1View(context: ViewContext) extends AbstractNoData1View {

  override def emptyState: Empty = Empty.defaultInstance

  override def processNothing(
    state: Empty,
    empty: Empty
  ): UpdateEffect[Empty] = effects.updateState(empty)

}
