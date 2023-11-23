package com.example.utils.iam.jwt.impl

import com.example.utils.iam.cryptography.RSAKeyPair

import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms.JwtRSAAlgorithm

/**
  * RSA Decoder. Can decode Json Web Tokens (JWTs).
  */
final case class RSADecoder(publicKey: RSAPublicKey) extends BaseJwtDecoder[RSAPublicKey] {
  val allowedAlgorithms = JwtAlgorithm.allRSA()
}

/**
  * RSA Codec. Can encode and decode Json Web Tokens (JWTs).
  */
final class RSACodec private (privateKey: RSAPrivateKey, val publicKey: RSAPublicKey, val algorithm: JwtRSAAlgorithm)
    extends BaseJwtCodec[RSAPublicKey, RSAPrivateKey](privateKey) {
  val allowedAlgorithms = JwtAlgorithm.allRSA()
}

object RSACodec {

  final def apply(privateKey: RSAPrivateKey, publicKey: RSAPublicKey, algorithm: JwtRSAAlgorithm): RSACodec =
    new RSACodec(privateKey, publicKey, algorithm)

  final def fromAlgorithmWithKeys(algoWithKeys: RSAKeyPair): RSACodec =
    new RSACodec(algoWithKeys.privateKey, algoWithKeys.publicKey, algoWithKeys.algorithm)

}
