package com.improving.utils.iam

import pdi.jwt.algorithms.JwtAsymmetricAlgorithm

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
