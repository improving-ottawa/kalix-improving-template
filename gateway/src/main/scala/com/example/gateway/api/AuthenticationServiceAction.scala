package com.example.gateway.api

import com.improving.extensions.oidc._
import com.example.gateway.utils.JwtIssuer
import com.improving.utils.FutureUtils

import com.google.api.HttpBody
import com.google.protobuf.empty.Empty
import com.improving.iam.AuthToken.toClaims
import io.circe.Json
import io.grpc.Status.{Code => StatusCode}
import kalix.scalasdk.Metadata
import kalix.scalasdk.MetadataEntry
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
      val code       = accessCodeData.code
      val stateToken = accessCodeData.state

      val redirectEffect =
        for {
          (identity, state) <- identityService.completeAuthorizationCodeFlow(code, stateToken)
          jwtToken          <- Future.fromEither(jwtIssuer.createJwtFor(identity))
          csrfToken          = state.csrfToken
        } yield {
          val httpHeaders = Metadata.empty
            .add("Cache-Control", "no-cache")
            .add("Set-Cookie", jwtIssuer.jwtToHttpCookie(jwtToken))
            .add("Set-Cookie", s"csrfToken=$csrfToken; Path=/")

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

  final def oidcCheck(empty: Empty): Action.Effect[HttpBody] = {
    val requestMetadata = actionContext.metadata.view.collect {
      case entry: MetadataEntry if Option(entry.value).nonEmpty => (entry.key, entry.value)
    }.toMap

    @inline def parseCookies(cookieValue: String) = {
      val cookies = cookieValue.split(';')
      cookies.view
        .map(_.split('='))
        .filter(_.length == 2)
        .map { parts => (parts.head.trim, parts.last.trim) }
        .filter { case (_, value) => value.nonEmpty }
        .toMap
        .map {
          case ("authToken", value) => ("authToken", parseAuthToken(value))
          case (key, value)         => (key, Json.fromString(value))
        }
    }

    @inline def parseAuthToken(authToken: String): Json = {
      val decoded  = jwtIssuer.decodeJwtToken(authToken).fold(throw _, identity)
      val jsonText = decoded.toClaims.toJson

      io.circe.parser.parse(decoded.toClaims.toJson).fold(throw _, identity)
    }

    val metadataJsonMap = requestMetadata.foldLeft(Map.empty[String, Json]) { case (acc, (key, value)) =>
      key match {
        case "Authorization" => acc + ("bearer_token" -> parseAuthToken(value.replace("Bearer", "").trim))
        case "Cookie"        => acc ++ parseCookies(value)
        case _               => acc
      }
    }

    val responseJson = Json.obj(metadataJsonMap.toSeq: _*)

    if (metadataJsonMap.isEmpty)
      effects.error("Invalid request, missing all authorization tokens.", StatusCode.UNAUTHENTICATED)
    else
      effects.reply(
        HttpBody(
          contentType = "application/json",
          data = com.google.protobuf.ByteString.copyFromUtf8(responseJson.spaces2)
        )
      )
  }

}
