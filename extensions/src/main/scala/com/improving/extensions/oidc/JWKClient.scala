package com.improving.extensions.oidc

import com.improving.utils.SystemClock

import cats._
import cats.data.OptionT
import cats.instances.future._
import cats.syntax.all._
import cats.effect._
import com.chatwork.scala.jwk._
import io.circe._
import sttp.client3._
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.client3.circe.asJson
import sttp.model.{StatusCode, Uri}

import scala.concurrent.{ExecutionContext, Future}

import java.time.ZoneId
import java.util.concurrent.atomic.AtomicReference

/** A caching [[JWKSet JWK set]] client, which can retrieve JWKs. */
sealed trait JWKClient[F[_]] {

  /**
    * Retrieves the [[JWKSet]] associated with the provided `clientId`, either from the `jwksUri` or from local cache.
    */
  def retrieveJwks(clientId: String, jwksUri: Uri): F[JWKSet]
}

object JWKClient {
  final private[oidc] type BackendResource[F[_]] = Resource[F, SttpBackend[F, _]]

  /** Get a new [[JWKClient]] for cats effect [[IO]]. */
  def catsEffect: JWKClient[IO] = {
    val freshCache: ClientCache[IO] = new CatsEffectCache(Ref.unsafe(Map.empty))
    new CatsEffectJWKClient(freshCache)
  }

  /** Get a new [[JWKClient]] for scala [[Future]]. */
  def scalaFuture(blockingContext: ExecutionContext): JWKClient[Future] = {
    val freshCache: ClientCache[Future] = new FutureCache(new AtomicReference(Map.empty))
    new FutureJWKClient(freshCache)(blockingContext)
  }

  /* Cache API */

  sealed trait ClientCache[F[_]] {
    def get(key: String): F[Option[JWKSet]]
    def put(key: String, jwkSet: JWKSet): F[Unit]
  }

  /* JWK Client Instances */

  private object CatsEffectJWKClient {
    // We only need one of these for all instances of `CatsEffectJWKClient`
    val backendResource: BackendResource[IO] = HttpClientCatsBackend.resource[IO]()
  }

  final private class CatsEffectJWKClient(cache: ClientCache[IO]) extends JWKClientImpl[IO](cache) {
    import CatsEffectJWKClient.backendResource

    protected def sendRequest[T, R](request: Request[T, R]): IO[Response[T]] =
      backendResource.use(backend => backend.send(request))

  }

  final private class FutureJWKClient(cache: ClientCache[Future])(implicit ec: ExecutionContext)
      extends JWKClientImpl[Future](cache) {
    private val backend = HttpClientFutureBackend()

    protected def sendRequest[T, R](request: Request[T, R]): Future[Response[T]] =
      backend.send(request)

  }

  /* Cache Instances */

  final private class CatsEffectCache(ref: Ref[IO, Map[String, JWKSet]]) extends ClientCache[IO] {
    def get(key: String): IO[Option[JWKSet]] = ref.modify(map => (map, map.get(key)))

    def put(key: String, jwkSet: JWKSet): IO[Unit] = ref.update(map => map.updated(key, jwkSet))
  }

  final private class FutureCache(ref: AtomicReference[Map[String, JWKSet]]) extends ClientCache[Future] {

    def get(key: String): Future[Option[JWKSet]] = {
      var result: Option[JWKSet] = None
      ref.updateAndGet { map =>
        result = map.get(key)
        map
      }
      Future.successful(result)
    }

    def put(key: String, jwkSet: JWKSet): Future[Unit] =
      Future.fromTry(
        scala.util.Try {
          ref.updateAndGet(map => map.updated(key, jwkSet))
          ()
        }
      )

  }

  /* Typed/specific errors */

  final case class JWKSetCreationError(message: String, cause: Option[JWKError]) extends scala.Error(message)

  object JWKEmptySetException extends scala.Error("Received empty set of JWK from OIDC Provider")

}

// Abstract/base implementation for `JWKClient`, including caching behavior.
abstract private class JWKClientImpl[F[_] : MonadThrow](cache: JWKClient.ClientCache[F]) extends JWKClient[F] {
  import JWKClient._

  // Depends on the particular `F[_]` implementation
  protected def sendRequest[T, R](request: Request[T, R]): F[Response[T]]

  final def retrieveJwks(clientId: String, jwksUri: Uri): F[JWKSet] =
    retrieveFromCache(clientId).flatMap {
      case Some(jwkSet) => MonadThrow[F].pure(jwkSet)
      case None         => retrieveAndCache(clientId, jwksUri)
    }

  final protected def retrieveFromCache(clientId: String): F[Option[JWKSet]] = {
    val resultT =
      for {
        cachedSet <- OptionT(cache.get(clientId))
        resultSet <- if (allJWKsAreValid(cachedSet)) OptionT.pure(cachedSet) else OptionT.none
      } yield resultSet

    resultT.value
  }

  final protected def retrieveAndCache(clientId: String, jwksUri: Uri): F[JWKSet] =
    for {
      jwkSet <- requestJwks(jwksUri)
      _      <- cache.put(clientId, jwkSet)
    } yield jwkSet

  final private def allJWKsAreValid(set: JWKSet): Boolean = {
    val nowZonedDateTime = SystemClock.currentDateTime.atZone(ZoneId.of("UTC"))
    set.breachEncapsulationOfValues.forall(jwk =>
      jwk.expireAt.fold(true)(expiresAt => expiresAt.isAfter(nowZonedDateTime))
    )
  }

  final private def requestJwks(jwksUri: Uri): F[JWKSet] = {
    import CommonErrors._

    @inline def handleJsonResponse(response: Json): F[JWKSet] =
      MonadThrow[F].fromEither {
        JWKSet.parseFromJson(response) match {
          case Left(error) => Left(JWKSetCreationError(error.message, error.cause))
          case Right(jwks) => if (jwks.size == 0) Left(JWKEmptySetException) else Right(jwks.toPublicJWKSet)
        }
      }

    @inline def handleResponseException(errors: ResponseException[String, Error]): F[JWKSet] =
      MonadThrow[F].raiseError(
        errors match {
          case HttpError(_, StatusCode.RequestTimeout)     => TimeoutOrServiceUnavailableError(jwksUri)
          case HttpError(_, StatusCode.ServiceUnavailable) => TimeoutOrServiceUnavailableError(jwksUri)
          case HttpError(body, code)                       => EndpointInvalidResponse(jwksUri, code.code, body)
          case DeserializationException(body, _)           => EndpointResponseNotJson(jwksUri, body)
        }
      )

    val request   = basicRequest.get(jwksUri).response(asJson[Json])
    val responseF = sendRequest(request)

    responseF.flatMap { response =>
      response.body.fold(
        error => handleResponseException(error),
        json => handleJsonResponse(json)
      )
    }
  }

}
