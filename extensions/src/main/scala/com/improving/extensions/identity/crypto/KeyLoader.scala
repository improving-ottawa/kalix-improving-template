package com.improving.extensions.identity.crypto

import pdi.jwt.algorithms._

import java.security.KeyFactory
import java.security.interfaces._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import scala.util.Try

sealed trait KeyLoader[Algo <: AlgorithmWithKeys] {
  final type Algorithm = Algo#Algorithm

  /* Public API */

  def load(
    jwtAlgorithm: Algorithm,
    publicKeyFilePath: String,
    privateKeyFilePath: String,
    privateKeyPassword: Option[String]
  ): Either[Throwable, Algo]

  final def loadFromConfig(jwtAlgorithm: Algorithm, config: KeyLoaderConfig): Either[Throwable, Algo] =
    load(jwtAlgorithm, config.publicKeyFilePath, config.privateKeyFilePath, config.privateKeyPassword)

  /* Internal API */
  protected def readFilePrivateKey(filePath: String, password: Option[String]): Either[Throwable, PKCS8EncodedKeySpec]

  protected def readFilePublicKey(filePath: String): Either[Throwable, X509EncodedKeySpec]

}

object KeyLoader {

  /** Load [[AlgorithmWithKeys JWT signing algorithm and keys]] from a [[KeyLoaderConfig configuration]]. */
  final def load(config: KeyLoaderConfig): Either[Throwable, AlgorithmWithKeys] =
    config.jwtAlgorithm match {
      case rsa: JwtRSAAlgorithm     => RSA.loadFromConfig(rsa, config)
      case ecdsa: JwtECDSAAlgorithm => ECDSA.loadFromConfig(ecdsa, config)
      case invalid                  => throw new RuntimeException(s"Cannot load key for JWT algorithm: $invalid")
    }

  object RSA extends KeyLoaderImpl with KeyLoader[RSAKeyPair] {
    final private val keyFactory = KeyFactory.getInstance("RSA", cryptoProvider)

    def load(
      jwtAlgo: JwtRSAAlgorithm,
      publicKeyFilePath: String,
      privateKeyFilePath: String,
      privateKeyPassword: Option[String]
    ): Either[Throwable, RSAKeyPair] =
      for {
        privKeySpec <- readFilePrivateKey(privateKeyFilePath, privateKeyPassword)
        pubKeySpec  <- readFilePublicKey(publicKeyFilePath)
        rsaPubKey   <- Try(keyFactory.generatePublic(pubKeySpec)).map(_.asInstanceOf[RSAPublicKey]).toEither
        rsaPrivKey  <- Try(keyFactory.generatePrivate(privKeySpec)).map(_.asInstanceOf[RSAPrivateKey]).toEither
      } yield RSAKeyPair(rsaPubKey, rsaPrivKey, jwtAlgo)

  }

  object ECDSA extends KeyLoaderImpl with KeyLoader[ECKeyPair] {
    final private val keyFactory = KeyFactory.getInstance("ECDSA", cryptoProvider)

    def load(
      jwtAlgo: JwtECDSAAlgorithm,
      publicKeyFilePath: String,
      privateKeyFilePath: String,
      privateKeyPassword: Option[String]
    ): Either[Throwable, ECKeyPair] =
      for {
        privKeySpec <- readFilePrivateKey(privateKeyFilePath, privateKeyPassword)
        pubKeySpec  <- readFilePublicKey(publicKeyFilePath)
        ecPubKey    <- Try(keyFactory.generatePublic(pubKeySpec)).map(_.asInstanceOf[ECPublicKey]).toEither
        ecPrivKey   <- Try(keyFactory.generatePrivate(privKeySpec)).map(_.asInstanceOf[ECPrivateKey]).toEither
      } yield ECKeyPair(ecPubKey, ecPrivKey, jwtAlgo)

  }

}
