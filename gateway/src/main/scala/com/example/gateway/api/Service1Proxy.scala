package com.example.gateway.api

import com.example.boundedContext.domain._
import com.example.common.api.JwtAuthorization
import kalix.scalasdk.action.Action

trait Service1Proxy extends GatewayProxyBase with JwtAuthorization {

  override def doNothing1(command: DoNothingCommand1): Action.Effect[DoNothingResponse1] =
    requiresAuthorization(
      effects.asyncReply(service1Client.doNothing(command))
    )

}
