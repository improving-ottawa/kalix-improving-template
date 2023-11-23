package com.example.utils.iam.jwt

import cats.effect.IO
import cats.syntax.all._
import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm

/**
  * Jwt configuration for OAuth2 IAM clients.
  */
case class JwtConfig(
  /** The [[JwtAsymmetricAlgorithm JWT asymmetric algorithm]] used to verify JWTs. */
  jwtAlgorithm: JwtAsymmetricAlgorithm,

  /** The path to the public key (`.pem` or `.der`) file. */
  publicKeyFilePath: String
)

object JwtConfig {

  def fromConfig(config: Config): IO[JwtConfig] = {
    val jwtAlgo   = getString("jwt-algorithm").map(JwtAlgorithm.fromString).map(_.asInstanceOf[JwtAsymmetricAlgorithm])
    val publicKey = getString("public-key-path")

    val readConfig = (
      jwtAlgo,
      publicKey,
    ).mapN(JwtConfig.apply)

    readConfig(config)
  }

}
