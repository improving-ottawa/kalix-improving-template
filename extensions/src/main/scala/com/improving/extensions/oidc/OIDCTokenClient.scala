package com.improving.extensions.oidc

import com.improving.extensions.oidc.internal.WebClient

import cats._
import cats.instances.future._
import cats.syntax.all._
import cats.effect._
import io.circe._
import sttp.model._
import sttp.client3._
import sttp.client3.circe.asJson

import scala.concurrent.{ExecutionContext, Future}

/**
  * Used to interact with a OIDC token endpoint during "Authorization Code" flow.
  *
  * @see
  *   [[https://darutk.medium.com/diagrams-of-all-the-openid-connect-flows-6968e3990660 this helpful website]] for an
  *   example diagram.
  */
sealed trait OIDCTokenClient[F[_]] {

  /**
    * Requests OIDC tokens from a `tokenEndpoint` with an `accessCode` from an "Authorization Code" flow.
    *
    * @param providerCallbackUri
    *   The original callback [[Uri uri]] sent to the OIDC provider when the "Authorization Code" flow was started.
    */
  def tokenRequest(tokenEndpoint: Uri, accessCode: String, providerCallbackUri: Uri): F[OIDCTokenResponse]

}

sealed abstract class OIDCTokenClientErrors { self: OIDCTokenClient.type =>

  // Token response from OIDC provider was invalid
  final case class InvalidTokenResponse(message: String, cause: Option[Throwable])
      extends scala.Error("Invalid OpenID Connect token response received: " + message, cause.orNull)

  // Token response indicated an error message
  final case class ProviderTokenResponseError(error: String)
      extends scala.Error(s"Received error from OIDC provider in response to a token request: $error")

}

object OIDCTokenClient extends OIDCTokenClientErrors {

  /** Get a new [[OIDCTokenClient]] for cats effect [[IO]]. */
  def catsEffect(config: OIDCClientConfig): OIDCTokenClient[IO] =
    new OIDCTokenClientBase[IO](config, WebClient.catsEffect)

  /** Get a new [[OIDCTokenClient]] for scala [[Future]]. */
  def scalaFuture(config: OIDCClientConfig, blockingContext: ExecutionContext)(implicit
    ec: ExecutionContext
  ): OIDCTokenClient[Future] =
    new OIDCTokenClientBase[Future](config, WebClient.scalaFuture(blockingContext))

}

final private class OIDCTokenClientBase[F[_]](config: OIDCClientConfig, client: WebClient[F])(implicit F: MonadThrow[F])
    extends OIDCTokenClient[F] {
  import OIDCTokenClient._

  private type TokenRequest = client.JsonRequest

  def tokenRequest(tokenEndpoint: Uri, authorizationCode: String, callbackUri: Uri): F[OIDCTokenResponse] = {
    val tokenRequest =
      buildTokenRequest(
        callbackUri,
        authorizationCode,
        tokenEndpoint,
        config.clientId,
        config.clientSecret
      )

    for {
      request  <- F.pure(tokenRequest)
      response <- submitTokenRequest(request)
    } yield response
  }

  private def submitTokenRequest(request: TokenRequest): F[OIDCTokenResponse] = {
    @inline def extractError(json: Json) =
      (json \\ "error").head.spaces2

    @inline def tryParseJson(json: Json) =
      F.fromEither(json.as[OIDCTokenResponse].leftMap(error => InvalidTokenResponse(json.spaces2, Some(error))))

    client.jsonRequest(request).flatMap {
      case json if (json \\ "error").nonEmpty => F.raiseError(ProviderTokenResponseError(extractError(json)))
      case json                               => tryParseJson(json)
    }
  }

  // Builds a OIDC token request
  private def buildTokenRequest(
    providerCallbackUri: Uri,
    authorizationCode: String,
    tokenEndpointUri: Uri,
    clientId: String,
    clientSecret: String
  ): TokenRequest = {
    val tokenRequestParameters: Map[String, String] =
      Map(
        // Same redirect URI we used in the code request
        "redirect_uri" -> providerCallbackUri.toString,
        // Grant type (= "authorization_code")
        "grant_type"   -> "authorization_code",
        // Code we received from the OIDC provider
        "code"         -> authorizationCode
      )

    basicRequest
      .body(tokenRequestParameters)
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .auth
      .basic(clientId, clientSecret)
      .post(tokenEndpointUri)
      .response(asJson[Json])
  }

}
