package com.example.gateway.api


import com.improving.extensions.oidc._
import com.example.gateway.utils.JwtIssuer
import com.improving.utils.FutureUtils

import com.google.api.HttpBody
import io.grpc.Status.{Code => StatusCode}
import kalix.scalasdk.Metadata
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

import scala.concurrent.Future

// This class was initially generated based on the .proto definition by Kalix tooling.

class AuthenticationServiceAction(
  identityService: OIDCIdentityService[Future],
  jwtIssuer: JwtIssuer,
  creationContext: ActionCreationContext
) extends AbstractAuthenticationServiceAction
    with FutureUtils {

  final def oidcAuthentication(request: BeginAuthenticationRequest): Action.Effect[HttpBody] = {
    val session      = OIDCState(request.providerId, request.csrfToken, request.redirectUri)
    val redirectUri  = identityService.beginAuthorizationCodeFlow(session)
    val redirectHtml = redirectUri.map { uri =>
      val html =
        s"""<head>
           |  <meta http-equiv="Refresh" content="0; URL=${uri.toString}" />
           |</head>""".stripMargin

      HttpBody(
        contentType = "text/html",
        data = com.google.protobuf.ByteString.copyFromUtf8(html)
      )
    }

    val asyncEffect =
      redirectHtml
        .map(httpBody => effects.reply(httpBody))
        .recover { case err: OIDCIdentityService.InvalidProviderIdError =>
          effects.error(err.getMessage, StatusCode.INVALID_ARGUMENT)
        }

    effects.asyncEffect(asyncEffect)
  }

  final def oidcCallback(accessCodeData: OIDCAccessCodeData): Action.Effect[HttpBody] =
    if (accessCodeData.code.isEmpty) {
      effects.error("No access `code` in query string", StatusCode.INVALID_ARGUMENT)
    } else if (accessCodeData.state.isEmpty) {
      effects.error("No session `state` in query string", StatusCode.INVALID_ARGUMENT)
    } else {
      val OIDCAccessCodeData(code, stateToken, _) = accessCodeData

      val redirectEffect =
        for {
          (identity, state) <- identityService.completeAuthorizationCodeFlow(code, stateToken)
          jwtToken          <- Future.fromEither(jwtIssuer.createJwtFor(identity))
          csrfToken          = state.csrfToken
        } yield {
          val httpHeaders = Metadata.empty
            .add("Set-Cookie", jwtIssuer.jwtToHttpCookie(jwtToken))
            .add("Set-Cookie", s"csrfToken=$csrfToken")

          val html =
            s"""<head>
               |  <meta http-equiv="Refresh" content="0; URL=${state.redirectUri}" />
               |</head>""".stripMargin

          effects.reply(
            HttpBody(
              contentType = "text/html",
              data = com.google.protobuf.ByteString.copyFromUtf8(html)
            ),
            httpHeaders
          )
        }

      effects.asyncEffect(redirectEffect)
    }

}
