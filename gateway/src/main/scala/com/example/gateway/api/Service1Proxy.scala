package com.example.gateway.api

import com.example.boundedContext.domain._
import com.improving.iam.KalixAuthorization
import kalix.scalasdk.action.Action

trait Service1Proxy extends GatewayProxyBase with KalixAuthorization {

  override def doNothing1(command: DoNothingCommand1): Action.Effect[DoNothingResponse1] =
      effects.asyncReply(service1Client.doNothing(command))

}
