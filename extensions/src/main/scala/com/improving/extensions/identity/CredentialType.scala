package com.improving.extensions.identity

import com.google.protobuf.ByteString

sealed trait CredentialType {
  def isPassword: Boolean
  def isOIDC: Boolean
}

object CredentialType {
  case object None extends CredentialType {
    final val isPassword = false
    final val isOIDC = false
  }

  final case class Password(salt: Array[Byte], password: Array[Byte]) extends CredentialType {
    final val isPassword = true
    final val isOIDC = false
  }

  final case class OIDC(providerName: String, linkedIdentityId: String) extends CredentialType {
    final val isPassword = false
    final val isOIDC = true
  }

  /* Protobuf conversion functions */

  private final type ProtoInnerType = CredentialTypeProto.LoginType
  private final val ProtoInnerType = CredentialTypeProto.LoginType

  final def toProto(credentialType: CredentialType): CredentialTypeProto = {
    val innerRecord: ProtoInnerType =
      credentialType match {
        case None                               => ProtoInnerType.Empty
        case OIDC(providerId, linkedIdentityId) => ProtoInnerType.Oidc(CredentialsOIDC(providerId, linkedIdentityId))
        case Password(salt, password)           => ProtoInnerType.Password(CredentialsPassword(ByteString.copyFrom(salt), ByteString.copyFrom(password)))
      }

    CredentialTypeProto(innerRecord)
  }

  final def fromProto(proto: CredentialTypeProto): CredentialType =
    proto.loginType match {
      case ProtoInnerType.Empty => None

      case ProtoInnerType.Oidc(CredentialsOIDC(providerName, linkedIdentityId, _)) =>
        OIDC(providerName, linkedIdentityId)

      case ProtoInnerType.Password(CredentialsPassword(salt, password, _)) =>
        Password(salt.toByteArray, password.toByteArray)
    }
}
