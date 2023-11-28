package com.improving.utils.iam

import pdi.jwt.algorithms._

import scala.util.Try

import java.security.KeyFactory
import java.security.interfaces._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

trait KeyLoader {

  /* Public API */

  /** Load [[AlgorithmWithKeys JWT signing algorithm and keys]] from a [[KeyLoaderConfig configuration]]. */
  final def load(config: KeyLoaderConfig): Either[Throwable, AlgorithmWithKeys] =
    loadKeys(
      config.jwtAlgorithm,
      config.publicKeyFilePath,
      config.privateKeyFilePath,
      config.privateKeyPassword
    )

  /* Internal API */
  protected def readFilePrivateKey(filePath: String, password: Option[String]): Either[Throwable, PKCS8EncodedKeySpec]

  protected def readFilePublicKey(filePath: String): Either[Throwable, X509EncodedKeySpec]

  protected def loadKeys(
    jwtAlgorithm: JwtAsymmetricAlgorithm,
    publicKeyFilePath: String,
    privateKeyFilePath: String,
    privateKeyPassword: Option[String]
  ): Either[Throwable, AlgorithmWithKeys]

}

object KeyLoader {

  /** Load [[AlgorithmWithKeys JWT signing algorithm and keys]] from a [[KeyLoaderConfig configuration]]. */
  def load(config: KeyLoaderConfig): Either[Throwable, AlgorithmWithKeys] =
    config.jwtAlgorithm match {
      case _: JwtRSAAlgorithm   => RSAKeyLoaderImpl.load(config)
      case _: JwtECDSAAlgorithm => ECKeyLoaderImpl.load(config)
      case invalid              => throw new RuntimeException(s"Cannot load key for JWT algorithm: $invalid")
    }

  private object RSAKeyLoaderImpl extends KeyLoaderImpl with KeyLoader {
    final private val keyFactory = KeyFactory.getInstance("RSA", cryptoProvider)

    protected def loadKeys(
      jwtAlgorithm: JwtAsymmetricAlgorithm,
      publicKeyFilePath: String,
      privateKeyFilePath: String,
      privateKeyPassword: Option[String]
    ): Either[Throwable, AlgorithmWithKeys] =
      for {
        privKeySpec <- readFilePrivateKey(privateKeyFilePath, privateKeyPassword)
        pubKeySpec  <- readFilePublicKey(publicKeyFilePath)
        rsaPubKey   <- Try(keyFactory.generatePublic(pubKeySpec)).map(_.asInstanceOf[RSAPublicKey]).toEither
        rsaPrivKey  <- Try(keyFactory.generatePrivate(privKeySpec)).map(_.asInstanceOf[RSAPrivateKey]).toEither
        jwtRsaAlgo  <- Try(jwtAlgorithm.asInstanceOf[JwtRSAAlgorithm]).toEither
      } yield RSAKeyPair(rsaPubKey, rsaPrivKey, jwtRsaAlgo)

  }

  private object ECKeyLoaderImpl extends KeyLoaderImpl with KeyLoader {
    final private val keyFactory = KeyFactory.getInstance("ECDSA", cryptoProvider)

    protected def loadKeys(
      jwtAlgorithm: JwtAsymmetricAlgorithm,
      publicKeyFilePath: String,
      privateKeyFilePath: String,
      privateKeyPassword: Option[String]
    ): Either[Throwable, AlgorithmWithKeys] =
      for {
        privKeySpec <- readFilePrivateKey(privateKeyFilePath, privateKeyPassword)
        pubKeySpec  <- readFilePublicKey(publicKeyFilePath)
        ecPubKey    <- Try(keyFactory.generatePublic(pubKeySpec)).map(_.asInstanceOf[ECPublicKey]).toEither
        ecPrivKey   <- Try(keyFactory.generatePrivate(privKeySpec)).map(_.asInstanceOf[ECPrivateKey]).toEither
        jwtECAlgo   <- Try(jwtAlgorithm.asInstanceOf[JwtECDSAAlgorithm]).toEither
      } yield ECKeyPair(ecPubKey, ecPrivKey, jwtECAlgo)

  }

}
