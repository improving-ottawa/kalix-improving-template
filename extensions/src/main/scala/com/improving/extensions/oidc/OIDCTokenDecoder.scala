package com.improving.extensions.oidc

import cats.data.Kleisli
import cats.implicits._
import com.chatwork.scala.jwk._
import com.chatwork.scala.jwk.JWKError.PublicKeyCreationError
import io.circe.Decoder
import io.circe.parser._
import pdi.jwt.{Jwt, JwtAlgorithm, JwtBase64}
import pdi.jwt.exceptions.JwtLengthException

import scala.util.Try

/**
 * Extracts and validates Jwt tokens from an OIDC token response, using the retrieved JWK set from the provider.
 */
sealed abstract class OIDCTokenDecoder private {
  import OIDCTokenDecoder._

  final def createDecoderUsingJWKS[P](jwkSet: JWKSet)(
    implicit decoder: Decoder[P]
  ): Kleisli[Either[Throwable, *], String, P] = Kleisli(token => decode(jwkSet)(token))

  final def decode[P](jwkSet: JWKSet)(token: String)(implicit decoder: Decoder[P]): Either[Throwable, P] =
    for {
      (header, payload) <- decodeToken(token)(decoder)
      _                 <- validateSignature(jwkSet, header, token)
    } yield payload

  // Validates the token, using the public key from the `JWKSet`
  private final def validateSignature(jwkSet: JWKSet, jwtHeader: OIDCJwtHeader, token: String) = {
    // Try to find the key ID (`kid`) in the jwkSet
    val keyForToken = Try(jwkSet.keyByKeyId(jwtHeader.keyId).get)
      .toEither
      .leftMap { _ =>
        val knownKeys = jwkSet.breachEncapsulationOfValues.view.flatMap(_.keyId).map(_.value).toSet
        JwkKeyNotFound(jwtHeader.keyId, knownKeys)
      }
      .flatMap {
        // RSA key
        case rsa: RSAJWK => Right(rsa)
        // Elliptic curve key
        case ec: ECJWK   => Right(ec)
        // Some other key
        case other       => Left(JwkNotAsymmetric(other))
      }

    for {
      jwk    <- keyForToken
      pubKey <- jwk.toPublicKey.leftMap(CouldNotExtractPublicKeyFromJwk)
      _      <- Try(Jwt.validate(token, pubKey, JwtAlgorithm.allAsymmetric)).toEither
    } yield ()
  }

  // Decodes the JWT header and payload (since we need the header for signature validation)
  private final def decodeToken[P](token: String)(implicit decoder: Decoder[P]) =
    for {
      (header, payload) <- splitToken(token)
      jwtHeader         <- parse(header).flatMap(_.as[OIDCJwtHeader])
      jwtPayload        <- parse(payload).flatMap(_.as[P])
    } yield (jwtHeader, jwtPayload)

  // Extracts the three parts of the ID token: header, payload, signature
  private final def splitToken(token: String) = {
    val parts = token.split('.')

    parts.length match {
      // We expect three parts
      case 3 =>
        val (header, payload) = (parts(0), parts(1))
        Try {
          (JwtBase64.decodeString(header), JwtBase64.decodeString(payload))
        }.toEither

      // Any other number of parts
      case _ =>
        Left(new JwtLengthException(s"Expected `id_token` [$token] to be composed of 3 parts separated by dots."))
    }
  }

}

object OIDCTokenDecoder extends OIDCTokenDecoder {

  /* Specific/typed errors */

  final case class JwkKeyNotFound(tokenKey: KeyId, providedKeys: Set[String])
    extends Error(s"OIDC token key id [$tokenKey] not found in provided JWK keys [${providedKeys.mkString(",")}]")

  final case class JwkNotAsymmetric(jwk: JWK)
    extends Error(s"Received unexpected non-asymmetric Json Web Key: $jwk")

  final case class CouldNotExtractPublicKeyFromJwk(cause: PublicKeyCreationError)
    extends Error(s"Could not extract public key from JWK due to error: ${cause.message}")

}
