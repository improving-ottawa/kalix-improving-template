package com.example.gateway.api

import com.example.service3.domain.{DoNothingCommand3, DoNothingResponse3}
import com.improving.iam.KalixAuthorization
import kalix.scalasdk.action.Action

trait Service3Proxy extends GatewayProxyBase with KalixAuthorization {

  override def doNothing3(command: DoNothingCommand3): Action.Effect[DoNothingResponse3] =
    effects.asyncReply(service3Client.doNothing(DoNothingCommand3.defaultInstance))

}
