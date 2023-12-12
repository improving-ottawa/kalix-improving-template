package com.example.gateway.api

import com.example.gateway.domain._
import com.example.gateway.middleware._

import kalix.scalasdk.action.Action

trait UserProxy extends GatewayProxyBase with AuthenticatedAction with AuthorizedAction {

  override def getUser(request: GetUserRequest): Action.Effect[GetUserResponse] =
    authenticatedEffect { authToken =>
      if (authToken.subject == request.userId || authToken.roles.contains("Admin"))
        effects.forward(components.userEntity.getUser(request))
      else
        effects.error("You are not authorized to perform this action.", io.grpc.Status.Code.PERMISSION_DENIED)
    }

  override def updateUserRoles(request: UpdateUserRolesRequest): Action.Effect[UserInfo] =
    authenticatedEffect {
      withRequiredRole("Admin") {
        effects.forward(components.userEntity.updateUserRoles(request))
      }
    }

}
