package com.example.gateway.entity

import com.improving.utils.SystemClock
import com.example.gateway.domain._

import com.google.protobuf.empty.Empty
import kalix.scalasdk.valueentity.ValueEntity
import kalix.scalasdk.valueentity.ValueEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.

class UserEntity(context: ValueEntityContext) extends AbstractUserEntity {
  override def emptyState: UserInfo = UserInfo(id = context.entityId)

  private def isUserInfoPopulated(state: UserInfo): Boolean =
    state.providerId.nonEmpty

  override def getUser(state: UserInfo, getUserRequest: GetUserRequest): ValueEntity.Effect[GetUserResponse] =
    if (isUserInfoPopulated(state))
      effects.reply(GetUserResponse(Some(state)))
    else
      effects.reply(GetUserResponse.defaultInstance)

  override def createOrUpdateUserInfo(state: UserInfo, userInfo: UserInfo): ValueEntity.Effect[Empty] = {
    val updatedState =
      state.copy(
        providerId = userInfo.providerId,
        emailAddress = userInfo.emailAddress,
        lastSynced = Some(SystemClock.currentInstant),
        givenName = userInfo.givenName,
        familyName = userInfo.familyName,
        preferredDisplayName = userInfo.preferredDisplayName,
        userRoles = userInfo.userRoles
      )

    effects
      .updateState(updatedState)
      .thenReply(Empty.defaultInstance)
  }

  override def updateUserRoles(state: UserInfo, request: UpdateUserRolesRequest): ValueEntity.Effect[UserInfo] =
    if (!isUserInfoPopulated(state))
      effects.error(s"User for ID `${request.userId}` not found.", io.grpc.Status.Code.NOT_FOUND)
    else {
      val updatedState = state.copy(userRoles = request.updatedUserRoles)
      effects
        .updateState(updatedState)
        .thenReply(updatedState)
    }

}
