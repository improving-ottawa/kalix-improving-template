package com.example.gateway.api

import com.example.service3.domain.{DoNothingCommand3, DoNothingResponse3}
import kalix.scalasdk.action.Action

trait Service3Proxy extends GatewayProxyBase with JwtAuthorization {

  override def doNothingService3(command: DoNothingCommand3): Action.Effect[DoNothingResponse3] =
    requiresAuthorization(
      effects.asyncReply(service3Client.doNothing(DoNothingCommand3.defaultInstance))
    )

}
