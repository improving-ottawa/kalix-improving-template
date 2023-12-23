package com.example.gateway.api

import com.example.gateway.domain._
import com.example.gateway.utils.JwtIssuer
import com.improving.extensions.oidc._
import com.improving.utils.{FutureUtils, SecureRandomString}

import com.google.api.HttpBody
import io.circe.Json
import io.circe.syntax._
import io.grpc.Status.{Code => StatusCode}
import kalix.scalasdk.Metadata
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext
import org.slf4j.LoggerFactory

import scala.concurrent.Future

// This class was initially generated based on the .proto definition by Kalix tooling.

class AuthenticationServiceAction(
  identityService: OIDCIdentityService[Future],
  jwtIssuer: JwtIssuer,
  creationContext: ActionCreationContext
) extends AbstractAuthenticationServiceAction
    with FutureUtils {

  private val log = LoggerFactory.getLogger(classOf[AuthenticationServiceAction])

  final def oidcAuthentication(request: BeginAuthenticationRequest): Action.Effect[HttpBody] = {
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

  final def oidcCallback(acd: OIDCAccessCodeData): Action.Effect[HttpBody] =
    oidcCallbackResponseInternal(acd.code, acd.state, generateCallbackResponse)

  final def oidcCompleteLogin(request: CompleteLoginRequest): Action.Effect[CompleteLoginResponse] =
    oidcCallbackResponseInternal(request.code, request.state, generateCompleteLoginResponse)

  /* Internal Implementation */

  final private type ResponseGen[T] = (String, String, Long, String, OIDCIdentity) => Action.Effect[T]

  private def oidcCallbackResponseInternal[T](
    code: String,
    stateToken: String,
    genResp: ResponseGen[T]
  ): Action.Effect[T] =
    if (code.isEmpty) {
      effects.error("No access `code` in query string", StatusCode.INVALID_ARGUMENT)
    } else if (stateToken.isEmpty) {
      effects.error("No session `state` in query string", StatusCode.INVALID_ARGUMENT)
    } else {
      val csrfToken = SecureRandomString(8)

      def syncUserIdentity(identity: OIDCIdentity, state: OIDCState) = {
        val userInfo = UserInfo(
          id = identity.id.toString,
          providerId = state.providerId,
          emailAddress = identity.email,
          lastSynced = None,
          givenName = identity.givenName.getOrElse(""),
          familyName = identity.familyName.getOrElse(""),
          preferredDisplayName = identity.preferredName.getOrElse(identity.name)
        )

        components.userEntity.createOrUpdateUserInfo(userInfo).execute()
      }

      val redirectEffect =
        for {
          (identity, state) <- identityService.completeAuthorizationCodeFlow(code, stateToken)
          (jwtToken, exp)   <- Future.fromEither(jwtIssuer.createJwtFor(identity, csrfToken))
          _                 <- syncUserIdentity(identity, state)
        } yield genResp(state.redirectUri, jwtToken, exp, csrfToken.toString, identity)

      effects.asyncEffect(redirectEffect)
    }

  private def generateCallbackResponse(
    redirectUri: String,
    jwt: String,
    expEpoch: Long,
    csrfToken: String,
    identity: OIDCIdentity
  ): Action.Effect[HttpBody] = {
    val httpHeaders = Metadata.empty
      .add("Cache-Control", "no-cache")
      .add("Set-Cookie", jwtIssuer.jwtToHttpCookie(jwt))

    val json = Json.obj(
      "redirectUri" -> redirectUri.asJson,
      "csrfToken"   -> csrfToken.asJson,
      "identity"    -> identity.asJson
    )

    val body = HttpBody(
      contentType = "application/json",
      data = com.google.protobuf.ByteString.copyFromUtf8(json.noSpaces)
    )

    effects.reply(body, httpHeaders)
  }

  private def generateCompleteLoginResponse(
    redirectUri: String,
    jwt: String,
    expEpoch: Long,
    csrfToken: String,
    identity: OIDCIdentity
  ): Action.Effect[CompleteLoginResponse] = {
    val httpHeaders = Metadata.empty
      .add("Cache-Control", "no-cache")
      .add("Set-Cookie", jwtIssuer.jwtToHttpCookie(jwt))

    val appIdentity = AppIdentity(
      identity.id.toString,
      identity.name,
      identity.preferredName.getOrElse(""),
      identity.familyName.getOrElse(""),
      identity.givenName.getOrElse(""),
      identity.middleName.getOrElse(""),
      identity.email.getOrElse("")
    )

    val response = CompleteLoginResponse(
      redirectUri,
      csrfToken,
      expEpoch,
      appIdentity
    )

    effects.reply(response, httpHeaders)
  }

}
