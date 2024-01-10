package com.example.gateway.api

import com.improving.extensions.identity._
import com.improving.extensions.oidc._
import com.improving.utils._

import com.example.gateway.domain._
import com.example.gateway.utils.JwtIssuer

import cats.syntax.all._
import com.google.api.HttpBody
import io.grpc.Status.{Code => StatusCode}
import kalix.scalasdk.Metadata
import kalix.scalasdk.action.Action
import org.slf4j.LoggerFactory

import scala.concurrent.Future

// This class was initially generated based on the .proto definition by Kalix tooling.

sealed abstract class AuthenticationServiceBase(final protected val jwtIssuer: JwtIssuer)
    extends AbstractAuthenticationService
    with FutureUtils {

  final protected val log = LoggerFactory.getLogger("com.example.gateway.api.AuthenticationService")

  protected def identityService: OIDCIdentityService[Future]

  protected def passwordUtility: PasswordUtility

  final protected type DataAndToken = (LoginData, String)

  final protected def createLoginDataAndToken(userIdentity: UserIdentity, redirect: String): Future[DataAndToken] = {
    val csrfToken = SecureRandomString(8)

    val resultEither = jwtIssuer
      .createJwtFor(userIdentity, csrfToken)
      .map { case (token, expiration) =>
        val loginData = LoginData(
          redirectUri = redirect,
          csrfToken = csrfToken.toString,
          sessionExpiration = expiration,
          userInfo = identityToInfo(userIdentity)
        )

        (loginData, token)
      }

    Future.fromEither(resultEither)
  }

  final private def identityToInfo(state: UserIdentity): UserInfo = {
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

sealed trait PasswordAuthenticationPart extends AuthenticationServiceBase {

  override def passwordLogin(request: PasswordAuthenticationRequest): Action.Effect[LoginData] = {
    def verifyPassword(userIdentity: UserIdentity): Future[Unit] =
      userIdentity.credentialType match {
        case CredentialType.Password(salt, hashedPassword) =>
          Future {
            if (!passwordUtility.verify(request.plaintextPassword, salt, hashedPassword))
              throw new Exception("Password verification failed.")
          }

        case _ =>
          val credentials = userIdentity.credentialType.toString
          Future.failed(new Exception(s"Attempted a password login against a user with $credentials credentials."))
      }

    val futureEffect =
      for {
        userIdentity     <- components.userEntity.getUser(GetUserRequest(request.userId)).execute()
        _                <- verifyPassword(userIdentity)
        (loginData, jwt) <- createLoginDataAndToken(userIdentity, request.redirectUri)
      } yield {
        val httpHeaders = Metadata.empty
          .add("Cache-Control", "no-cache")
          .add("Set-Cookie", jwtIssuer.jwtToHttpCookie(jwt))

        effects.reply(loginData)
      }

    effects.asyncEffect(
      futureEffect
        .onError(error => Future.successful(log.error(s"Login for ${request.userId} failed due to an error:", error)))
        .recover(_ => effects.error("Login failed."))
    )
  }

}

sealed trait OIDCAuthenticationPart extends AuthenticationServiceBase {

  final def oidcBeginLogin(request: BeginOIDCAuthenticationRequest): Action.Effect[HttpBody] = {
    val session        = OIDCState(request.providerId, request.redirectUri)
    val redirectUri    = identityService.beginAuthorizationCodeFlow(session)
    val redirectEffect = redirectUri.map { uri =>
      val emptyBody = HttpBody(
        contentType = "text/html",
        data = com.google.protobuf.ByteString.EMPTY
      )

      val headers = Metadata.empty
        .add("_kalix-http-code", "303")
        .add("Location", uri.toString)

      effects.reply(emptyBody, headers)
    }

    val asyncEffect =
      redirectEffect.recover { case err: OIDCIdentityService.InvalidProviderIdError =>
        effects.error(err.getMessage, StatusCode.INVALID_ARGUMENT)
      }

    effects.asyncEffect(asyncEffect)
  }

  final def oidcCompleteLogin(request: CompleteOIDCLoginRequest): Action.Effect[LoginData] =
    if (request.code.isBlank) {
      effects.error("No access `code` in query string", StatusCode.INVALID_ARGUMENT)
    } else if (request.state.isBlank) {
      effects.error("No session `state` in query string", StatusCode.INVALID_ARGUMENT)
    } else {
      val code       = request.code
      val stateToken = request.state

      val futureEffect =
        for {
          (oidcIdentity, state) <- identityService.completeAuthorizationCodeFlow(code, stateToken)
          userIdentity          <- getUserIdentityBySubject(state.providerId, oidcIdentity)
          (loginData, jwt)      <- createLoginDataAndToken(userIdentity, state.redirectUri)
        } yield {
          val httpHeaders = Metadata.empty
            .add("Cache-Control", "no-cache")
            .add("Set-Cookie", jwtIssuer.jwtToHttpCookie(jwt))

          effects.reply(loginData)
        }

      effects.asyncEffect(
        futureEffect
          .onError(error => Future.successful(log.error(s"OIDC login failed due to an error:", error)))
          .recover(_ => effects.error("Login failed."))
      )
    }

  final private def getUserIdentityBySubject(providerId: String, identity: OIDCIdentity) =
    components.userService
      .getUserIdBySubject(ForeignIdentityRequest(providerId, identity.id.toString))
      .execute()
      .flatMap {
        case ForeignIdentityResponse(Some(userId), _) =>
          for {
            userIdentity <- components.userEntity.getUser(GetUserRequest(userId)).execute()
            _            <- syncIdentityToUser(userId, providerId, identity)
          } yield userIdentity

        case _ => registerIdentityAsUser(providerId, identity)
      }

  final private def syncIdentityToUser(userId: String, providerId: String, identity: OIDCIdentity): Future[Unit] = {
    val userInformation = OIDCIdentityInformation(
      id = userId,
      providerId = providerId,
      subject = identity.id.toString,
      name = identity.preferredName.getOrElse(identity.name),
      familyName = identity.familyName.getOrElse(""),
      givenName = identity.givenName.getOrElse(""),
      middleName = identity.middleName.getOrElse(""),
      email = identity.email.getOrElse("")
    )

    components.userEntity
      .synchronizeOIDCIdentity(userInformation)
      .execute()
      .map(_ => ())
  }

  final private def registerIdentityAsUser(providerId: String, identity: OIDCIdentity): Future[UserIdentity] = {
    val registration = OIDCIdentityRegistration(
      providerId = providerId,
      subject = identity.id.toString,
      name = identity.preferredName.getOrElse(identity.name),
      email = identity.email.getOrElse(""),
      givenName = identity.givenName.getOrElse(""),
      familyName = identity.familyName.getOrElse("")
    )

    components.userService.registerOIDCIdentity(registration).execute()
  }

}

final class AuthenticationService(
  protected val identityService: OIDCIdentityService[Future],
  jwtIssuer: JwtIssuer,
  protected val passwordUtility: PasswordUtility
) extends AuthenticationServiceBase(jwtIssuer)
    with PasswordAuthenticationPart
    with OIDCAuthenticationPart
