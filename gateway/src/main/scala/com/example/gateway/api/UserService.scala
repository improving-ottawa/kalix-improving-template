package com.example.gateway.api

import com.improving.extensions.identity.UserIdentity

import com.example.gateway.domain._
import com.example.gateway.entity._
import com.example.gateway.utils.MiscUtils

import io.grpc.Status.{Code => StatusCode}
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext
import org.slf4j.LoggerFactory

import scala.concurrent.Future

// This class was initially generated based on the .proto definition by Kalix tooling.

final class UserService(context: ActionCreationContext) extends AbstractUserService {

  private val log = LoggerFactory.getLogger(classOf[UserService])

  // Various gRPC clients
  private lazy val loginInfoByEmailView = context.getGrpcClient(classOf[UserIdByEmailAddress], "gateway")
  private lazy val userIdByOIDCSubjectView = context.getGrpcClient(classOf[UserIdByOIDCSubject], "gateway")

  def getUserLoginInfo(request: UserEmailRequest): Action.Effect[UserLoginInfoResponse] =
    effects.asyncReply(
      loginInfoByEmailView.getUserLoginByEmailAddress(request)
        .map(resp => UserLoginInfoResponse(Some(resp)))
        .recover(_ => UserLoginInfoResponse(None))
    )

  def registerNewLocalUser(registration: LocalIdentityRegistration): Action.Effect[UserResponse] =
    effects.asyncEffect(
      // Check if there is already a user with the specified email address
      loginInfoByEmailView.getUserLoginByEmailAddress(UserEmailRequest(registration.emailAddress))
        .map(_ => true)
        .recover(_ => false)
        .flatMap {
          case false =>
            components.userEntity.registerLocalIdentity(registration).execute()
              .map { userInfo => effects.reply(UserResponse(Some(userInfo))) }
              .recover { error =>
                log.warn("An error occurred when invoking UserEntity `getUser`:", error)
                effects.reply(UserResponse(None))
              }

          case true =>
            val email = registration.emailAddress
            Future.successful(effects.error(s"User with email $email already registered.", StatusCode.ALREADY_EXISTS))
        }
    )

  def getUserIdBySubject(request: ForeignIdentityRequest): Action.Effect[ForeignIdentityResponse] = {
    effects.asyncReply(
      userIdByOIDCSubjectView.getUserIdBySubject(request)
        .map(resp => ForeignIdentityResponse(Some(resp.userId)))
        .recover(_ => ForeignIdentityResponse(None))
    )
  }

  def getUserInfo(request: GetUserRequest): Action.Effect[UserResponse] = {
    effects.asyncReply(
      components.userEntity.getUser(request).execute()
        .map(userInfo => UserResponse(Some(MiscUtils.identityToInfo(userInfo))))
        .recover { error =>
          log.warn("An error occurred when invoking UserEntity `getUser`:", error)
          UserResponse(None)
        }
    )
  }

  def updateLocalUserIdentity(request: UpdateLocalIdentityRequest): Action.Effect[UserResponse] =
    effects.asyncReply(
      components.userEntity.updateLocalIdentity(request).execute()
        .map(userInfo => UserResponse(Some(userInfo)))
        .recover { error =>
          log.warn("An error occurred when invoking UserEntity `updateLocalIdentity`:", error)
          UserResponse(None)
        }
    )

  def registerOIDCIdentity(registration: OIDCIdentityRegistration): Action.Effect[UserIdentity] = {
    @inline def getLoginInfoRelation(identity: UserIdentity) =
      UserLoginInfo(
        id = identity.id.toString,
        emailAddress = identity.emailAddress,
        loginType = MiscUtils.loginTypeOf(identity.credentialType),
        providerId = Some(registration.providerId)
      )

    @inline def getOIDCRelation(identity: UserIdentity) =
      ForeignIdentityUserIdRelation(
        providerId = registration.providerId,
        subject = registration.subject,
        userId = identity.id.toString
      )

    effects.asyncReply(
      for {
        userIdentity <- components.userEntity.registerOIDCIdentity(registration).execute()
        _            <- loginInfoByEmailView.registerRelation(getLoginInfoRelation(userIdentity))
        _            <- userIdByOIDCSubjectView.registerRelation(getOIDCRelation(userIdentity))
      } yield userIdentity
    )
  }
}
