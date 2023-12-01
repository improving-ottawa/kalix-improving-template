package com.improving.extensions.oidc

import com.improving.utils.SystemClock

import cats._
import cats.data.OptionT
import cats.instances.future._
import cats.syntax.all._
import cats.effect._
import com.chatwork.scala.jwk._
import io.circe._
import sttp.model.Uri

import scala.concurrent.{ExecutionContext, Future}

import java.time.ZoneId

import impl._

/** A caching [[JWKSet JWK set]] client, which can retrieve JWKs. */
sealed trait JWKClient[F[_]] {

  /**
    * Retrieves the [[JWKSet]] associated with the provided `clientId`, either from the `jwksUri` or from local cache.
    */
  def retrieveJwks(clientId: String, jwksUri: Uri): F[JWKSet]

}

sealed abstract class JWKClientErrors { self: JWKClient.type =>
  /* Typed/specific errors */

  final case class JWKSetCreationError(message: String, cause: Option[JWKError]) extends scala.Error(message)

  object JWKEmptySetException extends scala.Error("Received empty set of JWK from OIDC Provider")
}

object JWKClient extends JWKClientErrors {

  /** Get a new [[JWKClient]] for cats effect [[IO]]. */
  def catsEffect: JWKClient[IO] =
    new JWKClientImpl(InMemCache.catsEffect, WebClient.catsEffect)

  /** Get a new [[JWKClient]] for scala [[Future]]. */
  def scalaFuture(blockingContext: ExecutionContext)(implicit ec: ExecutionContext): JWKClient[Future] =
    new JWKClientImpl(InMemCache.scalaFuture, WebClient.scalaFuture(blockingContext))

}

// Implementation for `JWKClient`, including caching behavior.
private final class JWKClientImpl[F[_] : MonadThrow](cache: InMemCache[F], client: WebClient[F]) extends JWKClient[F] {
  import JWKClient._

  def retrieveJwks(clientId: String, jwksUri: Uri): F[JWKSet] =
    retrieveFromCache(clientId).flatMap {
      case Some(jwkSet) => MonadThrow[F].pure(jwkSet)
      case None         => retrieveAndCache(clientId, jwksUri)
    }

  def retrieveFromCache(clientId: String): F[Option[JWKSet]] = {
    val resultT: OptionT[F, JWKSet] =
      for {
        cachedSet <- OptionT(cache.getValue[JWKSet](cacheKey(clientId)))
        resultSet <- if (allJWKsAreValid(cachedSet)) OptionT.pure(cachedSet) else OptionT.none
      } yield resultSet

    resultT.value
  }

  protected def retrieveAndCache(clientId: String, jwksUri: Uri): F[JWKSet] =
    for {
      jwkSet <- requestJwks(jwksUri)
      _      <- cache.putValue(cacheKey(clientId), jwkSet)
    } yield jwkSet

  private def allJWKsAreValid(set: JWKSet): Boolean = {
    val nowZonedDateTime = SystemClock.currentDateTime.atZone(ZoneId.of("UTC"))
    set.breachEncapsulationOfValues.forall(jwk =>
      jwk.expireAt.fold(true)(expiresAt => expiresAt.isAfter(nowZonedDateTime))
    )
  }

  @inline private def cacheKey(clientId: String): String =
    "jwkset-" + clientId

  private def requestJwks(jwksUri: Uri): F[JWKSet] = {

    @inline def handleJsonResponse(response: Json): F[JWKSet] =
      MonadThrow[F].fromEither {
        JWKSet.parseFromJson(response) match {
          case Left(error) => Left(JWKSetCreationError(error.message, error.cause))
          case Right(jwks) => if (jwks.size == 0) Left(JWKEmptySetException) else Right(jwks.toPublicJWKSet)
        }
      }

    client.get(jwksUri) flatMap handleJsonResponse
  }

}
