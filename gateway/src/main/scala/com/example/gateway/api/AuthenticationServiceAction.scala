package com.example.gateway.api

import com.example.gateway.domain.UserInfo
import com.example.gateway.utils.JwtIssuer
import com.improving.extensions.oidc._
import com.improving.utils.{FutureUtils, SecureRandomString}

import com.google.api.HttpBody
import com.google.protobuf.empty.Empty
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
    val session        = OIDCState(request.providerId, request.redirectUri)
    val redirectUri    = identityService.beginAuthorizationCodeFlow(session)
    val redirectEffect = redirectUri.map { uri =>
      val html =
        s"""<head>
           |  <meta http-equiv="Refresh" content="0; URL=${uri.toString}" />
           |</head>""".stripMargin

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

  final def oidcCallback(accessCodeData: OIDCAccessCodeData): Action.Effect[HttpBody] =
    if (accessCodeData.code.isEmpty) {
      effects.error("No access `code` in query string", StatusCode.INVALID_ARGUMENT)
    } else if (accessCodeData.state.isEmpty) {
      effects.error("No session `state` in query string", StatusCode.INVALID_ARGUMENT)
    } else {
      val code       = accessCodeData.code
      val stateToken = accessCodeData.state
      val csrfToken  = SecureRandomString(8)

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
          jwtToken          <- Future.fromEither(jwtIssuer.createJwtFor(identity, csrfToken))
          _                 <- syncUserIdentity(identity, state)
        } yield {
          val csrfTokenCookie = s"csrfToken=${csrfToken.toString}; Path=/; SameSite=Lax"

          val httpHeaders = Metadata.empty
            .add("_kalix-http-code", "303")
            .add("Location", state.redirectUri)
            .add("Cache-Control", "no-cache")
            .add("Set-Cookie", jwtIssuer.jwtToHttpCookie(jwtToken))
            .add("Set-Cookie", csrfTokenCookie)

          val body = HttpBody(
            contentType = "text/html",
            data = com.google.protobuf.ByteString.EMPTY
          )

          effects.reply(body, httpHeaders)
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
        case "X-CSRF-Token"  => acc + ("csrfToken" -> Json.fromString(value))
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
