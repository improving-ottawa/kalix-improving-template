package com.improving.extensions.oidc.impl

import cats._
import cats.effect._
import cats.instances.future._
import cats.syntax.all._
import io.circe._
import sttp.client3._
import sttp.client3.circe.asJson
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.model.{StatusCode, Uri}

import scala.concurrent.{ExecutionContext, Future}

/** A functional Http web client used for making JSON requests. */
sealed trait WebClient[F[_]] {
  def get(uri: Uri): F[Json]

  def post(uri: Uri, body: Json): F[Json]
}

sealed abstract class WebClientErrors { self: WebClient.type =>
  /* Common Http related client errors */

  case class TimeoutOrServiceUnavailableError(uri: Uri)
    extends scala.Error(s"Endpoint `$uri` timeout or service unavailable.")

  case class EndpointResponseNotJson(uri: Uri, respBody: String)
    extends scala.Error(s"Endpoint `$uri` returned a non-Json response. Body: $respBody")

  case class EndpointInvalidResponse(uri: Uri, statusCode: Int, respBody: String)
    extends scala.Error(s"Endpoint `$uri` returned an unexpected (code: $statusCode) response: $respBody")
}

object WebClient extends WebClientErrors {

  /* Applicatives (constructors) */

  def catsEffect: WebClient[IO] = new CatsEffectWebClient

  def scalaFuture(implicit ec: ExecutionContext): WebClient[Future] = new ScalaFutureWebClient

  /* Resources */

  final private type BackendResource[F[_]] = Resource[F, SttpBackend[F, _]]

  // We only need one of these for all instances of `CatsEffectWebClient`
  final private val backendResource: BackendResource[IO] = HttpClientCatsBackend.resource[IO]()

  /* Instances */

  final private class CatsEffectWebClient extends WebClientImpl[IO] {
    protected def sendRequest[T](request: Request[T, _ >: PE]): IO[Response[T]] =
      backendResource.use(backend => backend.send(request))
  }

  final private class ScalaFutureWebClient(implicit ec: ExecutionContext) extends WebClientImpl[Future] {
    private val backend = HttpClientFutureBackend()

    protected def sendRequest[T](request: Request[T, _ >: PE]): Future[Response[T]] = backend.send(request)
  }

}

abstract private class WebClientImpl[F[+_]] protected (implicit F: MonadThrow[F]) extends WebClient[F] {
  import WebClient._

  final def get(uri: Uri): F[Json] = jsonRequestResponse(basicRequest.get(uri).response(asJson[Json]))

  final def post(uri: Uri, body: Json): F[Json] =
    jsonRequestResponse(
      basicRequest
        .post(uri)
        .body(body)(sttp.client3.circe.circeBodySerializer)
        .response(asJson[Json])
    )

  protected final type PE = AnyRef with sttp.capabilities.Effect[F]

  // Depends on the particular `F[_]` implementation
  protected def sendRequest[T](request: Request[T, _ >: PE]): F[Response[T]]

  // Send a Http client request and handle the expected (JSON) response
  private def jsonRequestResponse(request: Request[Either[ResponseException[String, Error], Json], Any]): F[Json] =
    sendRequest(request) flatMap { response =>
      response.body.fold(
        error => adaptResponseException(request.uri, error),
        resp  => F.pure(resp)
      )
    }

  // Adapt errors from Sttp client error results
  private def adaptResponseException[T](reqUri: Uri, respEx: ResponseException[String, Error]): F[T] =
    F.raiseError[T] {
      respEx match {
        case HttpError(_, StatusCode.RequestTimeout)     => TimeoutOrServiceUnavailableError(reqUri)
        case HttpError(_, StatusCode.ServiceUnavailable) => TimeoutOrServiceUnavailableError(reqUri)
        case HttpError(body, code)                       => EndpointInvalidResponse(reqUri, code.code, body)
        case DeserializationException(body, _)           => EndpointResponseNotJson(reqUri, body)
      }
    }

}
