package com.improving.extensions.identity.oidc

import io.circe._
import io.circe.syntax._
import sttp.model.Uri

/** Defines a sub-set of the metadata information that an OIDC provider's discovery endpoint can return. */
final case class DiscoveryMetadata(
  issuer: String,
  authorizationEndpoint: Uri,
  tokenEndpoint: Uri,
  userInfoEndpoint: Uri,
  jwksUri: Uri,
  scopesSupported: Set[String],
  claimsSupported: Set[String]
)

object DiscoveryMetadata {

  /** Circe [[Decoder decoder]] from a [[Json JSON]] object to [[DiscoveryMetadata]]. */
  implicit final val fromJson: Decoder[DiscoveryMetadata] =
    (c: HCursor) => {
      val parseUri = parseUriForCursor(c)(_)
      for {
        issuer     <- c.get[String]("issuer")
        authEP     <- c.get[String]("authorization_endpoint").flatMap(parseUri)
        tokenEP    <- c.get[String]("token_endpoint").flatMap(parseUri)
        userInfoEP <- c.get[String]("userinfo_endpoint").flatMap(parseUri)
        jwksUri    <- c.get[String]("jwks_uri").flatMap(parseUri)
        scopes     <- c.get[Set[String]]("scopes_supported")
        claims     <- c.get[Set[String]]("claims_supported")
      } yield DiscoveryMetadata(issuer, authEP, tokenEP, userInfoEP, jwksUri, scopes, claims)
    }

  /** Circe [[Encoder encoder]] from [[DiscoveryMetadata]] to a [[Json JSON]] object. */
  implicit final val toJson: Encoder[DiscoveryMetadata] =
    (meta: DiscoveryMetadata) =>
      Json.obj(
        "issuer"                 -> meta.issuer.asJson,
        "authorization_endpoint" -> meta.authorizationEndpoint.toString.asJson,
        "token_endpoint"         -> meta.tokenEndpoint.toString.asJson,
        "userinfo_endpoint"      -> meta.userInfoEndpoint.toString.asJson,
        "jwks_uri"               -> meta.jwksUri.toString.asJson,
        "scopes_supported"       -> meta.scopesSupported.asJson,
        "claims_supported"       -> meta.claimsSupported.asJson
      )

  // Helper function for parsing `Uri`s
  @inline final private def parseUriForCursor(c: HCursor)(str: String): Either[DecodingFailure, Uri] =
    try Right(Uri.unsafeParse(str))
    catch {
      case scala.util.control.NonFatal(err) => Left(DecodingFailure.fromThrowable(err, c.history))
    }

}
