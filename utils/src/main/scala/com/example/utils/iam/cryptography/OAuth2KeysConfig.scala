package com.example.utils.iam.cryptography

import cats.effect.IO
import cats.syntax.all._
import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm

/**
  * Crypto keys configuration for OAuth2 server.
  */
case class OAuth2KeysConfig(
  jwtAlgorithm: JwtAsymmetricAlgorithm,
  publicKeyFilePath: String,
  privateKeyFilePath: String,
  privateKeyPassword: Option[String]
)

object OAuth2KeysConfig {

  def fromConfig(config: Config): IO[OAuth2KeysConfig] = {
    val jwtAlgo      = getString("jwt-algorithm").map(JwtAlgorithm.fromString).map(_.asInstanceOf[JwtAsymmetricAlgorithm])
    val privateKey   = getString("private-key-path")
    val publicKey    = getString("public-key-path")
    val privPassword = getString("private-key-passphrase").optionally

    val readConfig = (
      jwtAlgo,
      publicKey,
      privateKey,
      privPassword
    ).mapN(OAuth2KeysConfig.apply)

    readConfig(config)
  }

}
