package com.example.gateway.middleware

import com.improving.iam.AuthToken
import com.improving.utils._
import com.example.gateway.utils.JwtIssuer

import akka.NotUsed
import akka.stream.scaladsl.Source
import cats.syntax.all._
import io.grpc.Status.{Code => StatusCode}
import kalix.scalasdk._
import kalix.scalasdk.action._

trait AuthenticatedAction extends Action {
  import AuthenticatedAction._

  protected def jwtIssuer: JwtIssuer

  private lazy val authTokenExtractor = parseMetadata(jwtIssuer)(_)

  final protected def authenticatedEffect[T](bodyFn: AuthToken => Action.Effect[T]): Action.Effect[T] =
    authTokenExtractor(actionContext.metadata).map(bodyFn) match {
      case Left((errorMsg, statusCode)) => effects.error(errorMsg, statusCode)
      case Right(result)                => result
    }

  final protected def authenticatedStreamingEffect[T](bodyStream: AuthToken => StreamingEffect[T]): StreamingEffect[T] =
    authTokenExtractor(actionContext.metadata).map(bodyStream) match {
      case Left((errorMsg, statusCode)) => Source.single(effects.error(errorMsg, statusCode))
      case Right(resultStream)          => resultStream
    }

}

object AuthenticatedAction {

  final protected type StreamingEffect[T] = Source[Action.Effect[T], NotUsed]

  final private def parseMetadata(jwtIssuer: JwtIssuer): Metadata => Either[(String, StatusCode), AuthToken] = {
    @inline def extractCsrfHeader(headers: Metadata) =
      headers.get("X-CSRF-Token") match {
        case None         => Left(("CSRF verification failed.", StatusCode.UNAUTHENTICATED))
        case Some(base64) =>
          Base64String.fromBase64String(base64).leftMap(_ => ("CSRF decoding failed.", StatusCode.UNAUTHENTICATED))
      }

    @inline def parseCookies(cookieValue: String) =
      cookieValue
        .split(';')
        .view
        .map(_.split('='))
        .filter(_.length == 2)
        .map { parts => (parts.head.trim, parts.last.trim) }
        .filter { case (key, value) => value.nonEmpty }
        .collectFirst { case ("authToken", value) =>
          value
        }

    @inline def extractAuthToken(headers: Metadata, csrfToken: Base64String) =
      headers.get("Cookie").flatMap(parseCookies) match {
        case None      => Left(("CSRF verification failed.", StatusCode.UNAUTHENTICATED))
        case Some(jwt) =>
          jwtIssuer
            .validateAndExtract(jwt, csrfToken)
            .leftMap(_ => ("JWT validation failed.", StatusCode.UNAUTHENTICATED))
      }

    metadata =>
      for {
        csrfToken <- extractCsrfHeader(metadata)
        authToken <- extractAuthToken(metadata, csrfToken)
      } yield authToken
  }

}
