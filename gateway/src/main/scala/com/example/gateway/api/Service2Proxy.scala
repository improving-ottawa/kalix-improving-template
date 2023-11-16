package com.example.gateway.api

import com.example.boundedContext.domain.{DoNothingCommand2, DoNothingResponse2}
import kalix.scalasdk.action.Action

trait Service2Proxy extends GatewayProxyBase with JwtAuthorization {

  override def doNothingService2(command: DoNothingCommand2): Action.Effect[DoNothingResponse2] =
    requiresAuthorization(
      effects.asyncReply(service2Client.doNothing(DoNothingCommand2.defaultInstance))
    )

}
