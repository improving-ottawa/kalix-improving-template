package com.improving.extensions.identity.oidc

import cats._
import cats.instances.future._
import cats.syntax.all._
import cats.effect._
import io.circe._
import sttp.model.Uri

import scala.concurrent.{ExecutionContext, Future}

import internal.WebClient

/** Used to retrieve OIDC `discovery` metadata from an OIDC discovery endpoint. */
sealed trait DiscoveryClient[F[_]] {

  /** Retrieves OIDC [[DiscoveryMetadata discovery metadata]] from an OIDC discovery endpoint. */
  def retrieveMetadata(discoveryUri: Uri): F[DiscoveryMetadata]
}

sealed abstract class DiscoveryClientErrors { self: DiscoveryClient.type =>

  /* Typed/specific errors */
  case class DiscoveryResponseError(endpoint: Uri, responseBody: String)
      extends scala.Error(s"Discovery endpoint '$endpoint' returned an invalid response: $responseBody")

}

object DiscoveryClient extends DiscoveryClientErrors {

  def catsEffect: DiscoveryClient[IO] = new DiscoveryClientImpl[IO](WebClient.catsEffect)

  def scalaFuture(blockingContext: ExecutionContext): DiscoveryClient[Future] = {
    implicit val futureMonadThrow: MonadThrow[Future] = catsStdInstancesForFuture(blockingContext)
    new DiscoveryClientImpl[Future](WebClient.scalaFuture(blockingContext))
  }

}

final private class DiscoveryClientImpl[F[_] : MonadThrow](client: WebClient[F]) extends DiscoveryClient[F] {
  import DiscoveryClient._

  def retrieveMetadata(discoveryUri: Uri): F[DiscoveryMetadata] = {

    @inline def handleJsonResponse(json: Json): F[DiscoveryMetadata] =
      MonadThrow[F].fromEither(
        DiscoveryMetadata.fromJson(json.hcursor) match {
          case ok @ Right(_) => ok
          case Left(_)       => Left(DiscoveryResponseError(discoveryUri, json.noSpaces))
        }
      )

    client.get(discoveryUri).flatMap(handleJsonResponse)
  }

}
