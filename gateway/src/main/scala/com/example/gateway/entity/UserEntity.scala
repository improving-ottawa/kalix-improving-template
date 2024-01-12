package com.example.gateway.entity

import com.improving.extensions.identity._
import com.improving.utils.SystemClock
import com.example.gateway.api._
import com.example.gateway.domain._
import com.improving.extensions.identity.password.PasswordService
import kalix.scalasdk.valueentity.ValueEntity
import kalix.scalasdk.valueentity.ValueEntityContext

import java.util.UUID

// This class was initially generated based on the .proto definition by Kalix tooling.

final class UserEntity(passwordUtility: PasswordService, context: ValueEntityContext) extends AbstractUserEntity {

  private def isUserInfoPopulated(state: UserIdentity): Boolean =
    state.credentialType != CredentialType.None

  private def optionalize(str: String): Option[String] =
    if (str.isBlank) None else Some(str)

  private def stateToInfo(state: UserIdentity): UserInfo = {
    val loginType = state.credentialType match {
      case CredentialType.OIDC(_, _) => LoginType.Oidc
      case _                         => LoginType.Password
    }

    UserInfo(
      id = state.id.toString,
      loginType = loginType,
      loginEmail = state.emailAddress,
      name = state.name,
      givenName = state.givenName.getOrElse(""),
      familyName = state.familyName.getOrElse(""),
      userRoles = state.roles
    )
  }

  def emptyState: UserIdentity =
    UserIdentity(
      id = UUID.fromString(context.entityId),
      name = "",
      credentialType = CredentialType.None
    )

  def getUser(state: UserIdentity, request: GetUserRequest): ValueEntity.Effect[UserIdentity] =
    if (isUserInfoPopulated(state))
      effects.reply(state)
    else
      effects.error(s"User for ID `${state.id}` not found.", io.grpc.Status.Code.NOT_FOUND)

  def registerOIDCIdentity(state: UserIdentity, registration: OIDCIdentityRegistration) =
    if (isUserInfoPopulated(state))
      effects.error(s"Tried to register an OIDC identity on top of an already existing user (${state.id})")
    else {
      val credentials = CredentialType.OIDC(registration.providerId, registration.subject)
      val newState    = state.copy(
        name = registration.name,
        credentialType = credentials,
        emailAddress = registration.email,
        givenName = optionalize(registration.givenName),
        familyName = optionalize(registration.familyName),
        lastUpdatedTimestamp = Some(SystemClock.currentInstant)
      )

      effects.updateState(newState).thenReply(newState)
    }

  def synchronizeOIDCIdentity(state: UserIdentity, identity: OIDCIdentityInformation) = {
    val newState = state.copy(
      name = identity.name,
      emailAddress = identity.email,
      givenName = optionalize(identity.givenName),
      familyName = optionalize(identity.familyName),
      lastUpdatedTimestamp = Some(SystemClock.currentInstant)
    )

    effects.updateState(newState).thenReply(stateToInfo(newState))
  }

  def registerLocalIdentity(state: UserIdentity, registration: LocalIdentityRegistration) =
    if (isUserInfoPopulated(state))
      effects.error(s"Tried to register a local identity on top of an already existing user (${state.id})")
    else {
      val PasswordService.Result(salt, hashedPassword) = passwordUtility.hashForStorage(registration.plaintextPassword)
      val credentials                                  = CredentialType.Password(salt, hashedPassword)
      val newState                                     = state.copy(
        name = s"${registration.givenName} ${registration.familyName}",
        credentialType = credentials,
        emailAddress = registration.emailAddress,
        givenName = optionalize(registration.givenName),
        familyName = optionalize(registration.familyName),
        lastUpdatedTimestamp = Some(SystemClock.currentInstant)
      )

      effects.updateState(newState).thenReply(stateToInfo(newState))
    }

  def updateLocalIdentity(state: UserIdentity, request: UpdateLocalIdentityRequest) =
    if (!isUserInfoPopulated(state))
      effects.error(s"User for ID `${request.id}` not found.", io.grpc.Status.Code.NOT_FOUND)
    else {
      val credentials = request.updatedPlaintextPassword.fold(state.credentialType) { text =>
        val PasswordService.Result(salt, hashedPassword) = passwordUtility.hashForStorage(text)
        CredentialType.Password(salt, hashedPassword)
      }

      val newState = state.copy(
        name = request.updatedName.getOrElse(state.name),
        emailAddress = request.updatedUserEmail.getOrElse(state.emailAddress),
        givenName = request.updatedFirstName.orElse(state.givenName),
        familyName = request.updatedLastName.orElse(state.familyName),
        lastUpdatedTimestamp = Some(SystemClock.currentInstant)
      )

      effects.updateState(newState).thenReply(stateToInfo(newState))
    }

  def updateUserRoles(state: UserIdentity, request: UpdateUserRolesRequest) =
    if (!isUserInfoPopulated(state))
      effects.error(s"User for ID `${request.userId}` not found.", io.grpc.Status.Code.NOT_FOUND)
    else {
      val updatedState = state.copy(roles = request.updatedUserRoles)
      effects
        .updateState(updatedState)
        .thenReply(updatedState)
    }

}
