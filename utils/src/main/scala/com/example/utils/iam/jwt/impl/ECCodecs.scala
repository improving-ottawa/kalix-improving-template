package com.example.utils.iam.jwt.impl

import java.security.interfaces.{ECPrivateKey, ECPublicKey}
import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms.JwtECDSAAlgorithm
import com.example.utils.iam.cryptography._

/**
  * Elliptic-Curve Decoder. Can decode Json Web Tokens (JWTs).
  */
final case class ECDecoder(publicKey: ECPublicKey) extends BaseJwtDecoder[ECPublicKey] {
  val allowedAlgorithms = JwtAlgorithm.allECDSA()
}

/**
  * Elliptic-Curve Codec. Can encode and decode Json Web Tokens (JWTs).
  */
final class ECCodec private (privateKey: ECPrivateKey, val publicKey: ECPublicKey, val algorithm: JwtECDSAAlgorithm)
    extends BaseJwtCodec[ECPublicKey, ECPrivateKey](privateKey) {
  val allowedAlgorithms = JwtAlgorithm.allECDSA()
}

object ECCodec {

  final def apply(privateKey: ECPrivateKey, publicKey: ECPublicKey, algorithm: JwtECDSAAlgorithm): ECCodec =
    new ECCodec(privateKey, publicKey, algorithm)

  final def fromAlgorithmWithKeys(algoWithKeys: ECKeyPair): ECCodec =
    new ECCodec(algoWithKeys.privateKey, algoWithKeys.publicKey, algoWithKeys.algorithm)

}
