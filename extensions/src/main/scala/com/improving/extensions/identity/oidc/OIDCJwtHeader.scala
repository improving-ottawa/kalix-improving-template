package com.improving.extensions.identity.oidc

import com.chatwork.scala.jwk.KeyId
import io.circe.{Decoder, HCursor}
import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm

/**
  * A standard OpenID Connect JWT header, which all OIDC JWTs should provide.
  *
  * @param algorithm
  *   The [[JwtAsymmetricAlgorithm]] algorithm used to sign the token.
  * @param keyId
  *   The [[KeyId]] of the public key used to verify the signature
  */
final case class OIDCJwtHeader(algorithm: JwtAsymmetricAlgorithm, keyId: KeyId)

object OIDCJwtHeader {

  implicit final val fromJson: Decoder[OIDCJwtHeader] =
    (c: HCursor) =>
      for {
        algo  <- c.get[String]("alg").map(JwtAlgorithm.fromString).map(_.asInstanceOf[JwtAsymmetricAlgorithm])
        _     <- c.get[String]("typ").map(tokenType => require(tokenType == "JWT"))
        keyId <- c.get[String]("kid").map(KeyId(_))
      } yield OIDCJwtHeader(algo, keyId)

}
