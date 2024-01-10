package com.example.gateway.api

import com.example.gateway.domain.LocalIdentityRegistration
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class UserService(creationContext: ActionCreationContext) extends AbstractUserService {

  override def getUserLoginInfo(userEmailRequest: UserEmailRequest): Action.Effect[UserLoginInfoResponse] = {
    throw new RuntimeException("The command handler for `GetUserLoginInfo` is not implemented, yet")
  }

  override def registerNewLocalUser(
    localIdentityRegistration: LocalIdentityRegistration
  ): Action.Effect[UserResponse] = {
    throw new RuntimeException("The command handler for `RegisterNewLocalUser` is not implemented, yet")
  }

  override def getUserIdBySubject(
    foreignIdentityRequest: ForeignIdentityRequest
  ): Action.Effect[ForeignIdentityResponse] = {
    throw new RuntimeException("The command handler for `GetUserIdBySubject` is not implemented, yet")
  }

  override def getUserInfo(getUserRequest: GetUserRequest): Action.Effect[UserResponse]                   = {
    throw new RuntimeException("The command handler for `GetUserInfo` is not implemented, yet")
  }

  override def updateLocalUserIdentity(
    updateLocalIdentityRequest: UpdateLocalIdentityRequest
  ): Action.Effect[UserResponse] = {
    throw new RuntimeException("The command handler for `UpdateLocalUserIdentity` is not implemented, yet")
  }

}
