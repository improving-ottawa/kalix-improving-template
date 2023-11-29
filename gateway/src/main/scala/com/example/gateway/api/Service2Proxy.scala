package com.example.gateway.api

import com.example.boundedContext.domain.{DoNothingCommand2, DoNothingResponse2}
import com.improving.iam.KalixAuthorization
import kalix.scalasdk.action.Action

trait Service2Proxy extends GatewayProxyBase with KalixAuthorization {

  override def doNothing2(command: DoNothingCommand2): Action.Effect[DoNothingResponse2] =
      effects.asyncReply(service2Client.doNothing(DoNothingCommand2.defaultInstance))

}
