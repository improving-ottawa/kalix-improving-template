package com.example.gateway.utils

import com.improving.extensions.identity._

import com.example.gateway.domain._

object MiscUtils {

  final def loginTypeOf(ct: CredentialType): LoginType =
    ct match {
      case CredentialType.OIDC(_, _) => LoginType.Oidc
      case _                         => LoginType.Password
    }

  final def identityToInfo(state: UserIdentity): UserInfo = {
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

}
