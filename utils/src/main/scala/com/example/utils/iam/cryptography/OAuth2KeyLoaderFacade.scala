package com.example.utils.iam.cryptography

import java.security.PublicKey

import com.example.utils.iam.jwt.JwtConfig
import cats.effect.IO
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm

trait OAuth2KeyLoaderFacade {

  /**
    * Method for loading keys through Files
    */
  def loadKeysFromFiles(
    jwtAlgorithm: JwtAsymmetricAlgorithm,
    publicKeyFilePath: String,
    privateKeyFilePath: String,
    privateKeyPassword: Option[String]
  ): IO[AlgorithmWithKeys]

  /**
    * Loads a public key from a file path.
    */
  def loadPublicKeyFromFile(jwtAlgorithm: JwtAsymmetricAlgorithm, publicKeyFilePath: String): IO[PublicKey]

  /**
    * Method for loading keys through provided config
    */
  def loadKeysFromConfig(config: OAuth2KeysConfig): IO[AlgorithmWithKeys]

  /**
    * Loads a public key from the provided [[JwtConfig configuration]].
    */
  def loadPublicKeyFromConfig(config: JwtConfig): IO[PublicKey]
}
