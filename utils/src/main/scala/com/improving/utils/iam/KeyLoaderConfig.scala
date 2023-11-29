package com.improving.utils.iam

import pdi.jwt.algorithms._

/**
 * A configuration used by the [[KeyLoader]] for loading cryptographic keys when operating with Json Web Tokens (JWTs).
 *
 * @param jwtAlgorithm The [[JwtAsymmetricAlgorithm JWT asymmetric algorithm]] used to verify JWTs.
 *                     Note: can be `either` a [[JwtRSAAlgorithm]] `or` a [[JwtECDSAAlgorithm]], and it __must__ match
 *                     the algorithm used when the public/private keys were created.
 *
 * @param publicKeyFilePath The path to the __public__ key (`.pem` or `.der`) file.
 * @param privateKeyFilePath The path to the __private__ key (`.pem` or `.der`) file.
 * @param privateKeyPassword (Optional) A secret key used for decrypting the private key file.
 */
case class KeyLoaderConfig(
  /** The [[JwtAsymmetricAlgorithm JWT asymmetric algorithm]] used to verify JWTs. */
  jwtAlgorithm: JwtAsymmetricAlgorithm,

  /** The path to the __public__ key (`.pem` or `.der`) file. */
  publicKeyFilePath: String,

  /** The path to the __private__ key (`.pem` or `.der`) file. */
  privateKeyFilePath: String,

  /** (Optional) A secret key used for decrypting the private key file. */
  privateKeyPassword: Option[String]
)

object KeyLoaderConfig {

  // TODO: Load this from a Typesafe `Config` record (later)

}
