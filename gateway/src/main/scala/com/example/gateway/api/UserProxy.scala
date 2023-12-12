package com.example.gateway.api

import com.example.gateway.domain.{GetUserRequest, GetUserResponse}
import com.example.gateway.middleware._
import kalix.scalasdk.action.Action

trait UserProxy extends GatewayProxyBase with AuthenticatedAction {

  override def getUser(request: GetUserRequest): Action.Effect[GetUserResponse] =
    authenticatedEffect { authToken =>
      if (authToken.subject != request.userId)
        effects.error("You are not authorized to perform this action.", io.grpc.Status.Code.PERMISSION_DENIED)
      else
        effects.forward(components.userEntity.getUser(request))
    }

}
