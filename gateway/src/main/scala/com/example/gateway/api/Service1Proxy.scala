package com.example.gateway.api

import com.example.boundedContext.domain._
import kalix.scalasdk.action.Action

trait Service1Proxy extends GatewayProxyBase with JwtAuthorization {
  override def doNothingService1(command: DoNothingCommand1): Action.Effect[DoNothingResponse1] =
    requiresAuthorization(
      effects.asyncReply(service1Client.doNothing(command))
    )

}
