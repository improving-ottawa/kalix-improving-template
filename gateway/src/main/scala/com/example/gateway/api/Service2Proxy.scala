package com.example.gateway.api

import com.example.boundedContext.domain.{DoNothingCommand2, DoNothingResponse2}
import com.example.common.api.JwtAuthorization
import kalix.scalasdk.action.Action

trait Service2Proxy extends GatewayProxyBase with JwtAuthorization {

  override def doNothing2(command: DoNothingCommand2): Action.Effect[DoNothingResponse2] =
    requiresAuthorization(
      effects.asyncReply(service2Client.doNothing(DoNothingCommand2.defaultInstance))
    )

}
