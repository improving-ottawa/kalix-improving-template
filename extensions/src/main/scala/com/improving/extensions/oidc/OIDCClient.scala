package com.improving.extensions.oidc

import com.improving.extensions.oidc.internal._
import com.improving.utils._

import cats._
import cats.arrow.FunctionK
import cats.data.Kleisli
import cats.instances.future._
import cats.syntax.all._
import cats.effect._
import com.chatwork.scala.jwk._
import sttp.model._
import scalapb.GeneratedMessage

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/** An OpenID Connect [[OIDCClient client]], which can be used for the OIDC "Authorization Code" flow. */
sealed trait OIDCClient[F[_]] {

  /** The [[OIDCClientConfig client configuration]] used with this client. */
  def config: OIDCClientConfig

  /** The OIDC provider callback [[Uri uri]] (to this service/server). */
  def providerCallbackUri: Uri

  /** Gets (if cached) or retrieves the OIDC [[DiscoveryMetadata discovery metadata]] from the OIDC provider. */
  def getMetadata: F[DiscoveryMetadata]

  /** Starts an OIDC "Authorization Code" flow, returning the client (UI) redirect [[Uri uri]]. */
  def beginAuthentication(state: String): F[Uri]

  /** Completes the "Authorization Code" flow, given the `code` and `state` (__sessionKey__) from the OIDC provider. */
  def completeAuthentication(code: String): F[OIDCIdentity]

}

// Specific / typed errors for `OIDCClient`
sealed abstract class OIDCClientErrors { self: OIDCClient.type =>

  // CSRF Rejection, due to missing authorization session for the received key.
  final case class CsrfRejectionError(key: Base64String)
      extends scala.Error(s"Authorization session not found for key: ${key.toString}")

  // Authorization code callback from OIDC provider was missing `code` or `state` fields.
  final case class MissingAuthorizationCodeOrState(uri: Uri)
      extends scala.Error(s"Missing authorization code or state in URI: $uri")

}

object OIDCClient extends OIDCClientErrors with OIDCClientUtils {

  /** Create a new [[OIDCClient]], which can be used for the OIDC "Authorization Code" flow. */
  final def apply[F[_]](config: OIDCClientConfig, providerCallbackUri: Uri)(
    implicit effect: SupportedEffect[F]
  ): OIDCClient[F] =
    new OIDCClientImpl[F](
      config,
      providerCallbackUri,
      effect.jwkClient,
      effect.metadataClient,
      effect.metadataCache,
      effect.tokenClient(config)
    )(effect.monadThrow)

  /* Implementation Details */

  /** Supported effect types for [[OIDCClient `OIDCClient[Effect[_]]` ]] */
  sealed trait SupportedEffect[F[_]] {
    private[oidc] def monadThrow: MonadThrow[F]
    private[oidc] def jwkClient: JWKClient[F]
    private[oidc] def metadataClient: DiscoveryClient[F]
    private[oidc] def metadataCache: InMemCache[F]
    private[oidc] def tokenClient(config: OIDCClientConfig): OIDCTokenClient[F]
  }

  object SupportedEffect {

    implicit final val catsEffectIsSupportedEffect: SupportedEffect[IO] =
      new SupportedEffect[IO] {
        final def monadThrow: MonadThrow[IO]                                 = MonadThrow[IO]
        final def jwkClient: JWKClient[IO]                                   = JWKClient.catsEffect
        final def metadataClient: DiscoveryClient[IO]                        = DiscoveryClient.catsEffect
        final def metadataCache: InMemCache[IO]                              = InMemCache.catsEffect
        final def tokenClient(config: OIDCClientConfig): OIDCTokenClient[IO] = OIDCTokenClient.catsEffect(config)
      }

    implicit final def scalaFutureIsSupportedEffect(implicit asyncContext: AsyncContext): SupportedEffect[Future] =
      new SupportedEffect[Future] {
        implicit private val ec: ExecutionContext         = asyncContext.executionContext
        final def monadThrow: MonadThrow[Future]          = MonadThrow[Future]
        final def jwkClient: JWKClient[Future]            = JWKClient.scalaFuture(asyncContext.blockingContext)
        final def metadataClient: DiscoveryClient[Future] = DiscoveryClient.scalaFuture(asyncContext.blockingContext)
        final def metadataCache: InMemCache[Future]       = InMemCache.scalaFuture(ec)

        final def tokenClient(config: OIDCClientConfig): OIDCTokenClient[Future] =
          OIDCTokenClient.scalaFuture(config, asyncContext.blockingContext)

      }

  }

}

// Utility functions for `OIDCClient`
sealed trait OIDCClientUtils { self: OIDCClient.type =>

  // Parses the `state` key and authorization `code` from a `redirectUri`
  final def parseStateAndAuthorizationCodeFrom(uri: Uri): Either[Throwable, (Base64String, String)] = {
    val srcMap = uri.fragment
      .map { fragText =>
        // It appears that STTP does not have Query fragment parsing...?
        // TODO: Look into replacing this with STTP `UriInterpolator` somehow
        akka.http.scaladsl.model.Uri.Query(fragText).toMap
      }
      .getOrElse {
        uri.querySegments.collect { case Uri.QuerySegment.KeyValue(key, value, _, _) =>
          (key, value)
        }.toMap
      }

    @inline def getOrError(key: String): Either[Throwable, String] =
      srcMap.get(key).map(Right(_)).getOrElse(Left(MissingAuthorizationCodeOrState(uri)))

    for {
      code  <- getOrError("code")
      stKey <- getOrError("state").map(Base64String.unsafeFromBase64String(_))
    } yield (stKey, code)
  }

}

// `OIDCClient` Implementation
final private class OIDCClientImpl[F[_]](
  val config: OIDCClientConfig,
  val providerCallbackUri: Uri,
  jwkClient: JWKClient[F],
  metadataClient: DiscoveryClient[F],
  metadataCache: InMemCache[F],
  tokenClient: OIDCTokenClient[F])(
  implicit F: MonadThrow[F]
) extends OIDCClient[F] {

  /* OIDCClient Implementation */

  val getMetadata: F[DiscoveryMetadata] = {
    val cacheKey = s"${config.clientId}-metadata"

    @inline def retrieveAndCacheMetadata =
      for {
        metaUri  <- F.fromTry(scala.util.Try(Uri.unsafeParse(config.discoveryUri)))
        metadata <- metadataClient.retrieveMetadata(metaUri)
        _        <- metadataCache.putValue(cacheKey, metadata)
      } yield metadata

    metadataCache.getValue[DiscoveryMetadata](cacheKey).flatMap {
      case Some(metadata) => F.pure(metadata)
      case None           => retrieveAndCacheMetadata
    }
  }

  def beginAuthentication(state: String): F[Uri] =
    for {
      metadata    <- getMetadata
      redirectUri <- beginAuthenticationInternal(metadata, state)
    } yield redirectUri

  def completeAuthentication(code: String): F[OIDCIdentity] = {
    for {
      metadata <- getMetadata
      identity <- completeAuthenticationInternal(metadata, code)
    } yield identity
  }


  /* Internal Implementation */

  private val requiredScopes = OIDCIdentity.requiredScopes.mkString(" ")

  private lazy val getJWKSet: F[JWKSet] =
    getMetadata.flatMap { metadata =>
      jwkClient.retrieveJwks(config.clientId, metadata.jwksUri)
    }

  private lazy val idTokenDecoder: Kleisli[F, OIDCTokenResponse, OIDCIdentity] = {
    val arrow: Either[Throwable, *] ~> F =
      new FunctionK[Either[Throwable, *], F] {
        final def apply[A](fa: Either[Throwable, A]): F[A] = F.fromEither(fa)
      }

    Kleisli.liftF(getJWKSet).flatMap { jwkSet =>
      OIDCTokenDecoder
        .createDecoderUsingJWKS[OIDCIdentity](jwkSet)
        .mapK(arrow)
        .compose((response: OIDCTokenResponse) => F.pure(response.idToken))
    }
  }

  protected def beginAuthenticationInternal(metadata: DiscoveryMetadata, state: String): F[Uri] =
    F.fromTry {
      scala.util.Try {
        // Nonce
        val nonce = SecureRandomString(16)

        // Additional query string parameters
        val additionalParams: Map[String, String] = config.codeFlowParams

        // Required query string parameters for the request
        val requestParameters = Map(
          // Code grant request
          "response_type" -> "code",
          // Client Id
          "client_id"     -> config.clientId,
          // Redirect URL
          "redirect_uri"  -> providerCallbackUri.toString,
          // Requested scopes
          "scope"         -> requiredScopes,
          // State
          "state"         -> state,
          // Nonce
          "nonce"         -> nonce.toString
        ) ++ additionalParams

        val uriQuery: Seq[Uri.QuerySegment] =
          requestParameters.map { case (key, value) => Uri.QuerySegment.KeyValue(key, value) }.toSeq

        metadata.authorizationEndpoint.addQuerySegments(uriQuery)
      }
    }

  private def completeAuthenticationInternal(metadata: DiscoveryMetadata, code: String): F[OIDCIdentity] =
    for {
      response       <- tokenClient.tokenRequest(metadata.tokenEndpoint, code, providerCallbackUri)
      parsedIdentity <- idTokenDecoder.run(response)
    } yield parsedIdentity

}
