package com.example.utils.iam.cryptography.impl

import java.security.PublicKey
import java.security.interfaces.{ECPrivateKey, ECPublicKey, RSAPrivateKey, RSAPublicKey}
import cats.effect.IO
import com.example.utils.iam.cryptography._
import com.example.utils.iam.jwt.JwtConfig
import pdi.jwt.algorithms.{JwtAsymmetricAlgorithm, JwtECDSAAlgorithm, JwtRSAAlgorithm}

object DefaultOAuth2KeyLoaderImpl extends OAuth2KeyLoaderFacade {

  private type PublicLoaderFunc[A]  = (String => Either[Throwable, A]) => IO[A]
  private type PrivateLoaderFunc[A] = ((String, Option[String]) => Either[Throwable, A]) => IO[A]

  override def loadKeysFromFiles(
    jwtAlgorithm: JwtAsymmetricAlgorithm,
    publicKeyFilePath: String,
    privateKeyFilePath: String,
    privateKeyPassword: Option[String]
  ): IO[AlgorithmWithKeys] = {
    def loadPublic[A]: PublicLoaderFunc[A] =
      errorIfMissingOrLoadUsing("publicKeyFilePath")(publicKeyFilePath)(_)

    def loadPrivate[A]: PrivateLoaderFunc[A] = { fun =>
      errorIfMissingOrLoadUsing("privateKeyFilePath")(privateKeyFilePath)(fun(_, privateKeyPassword))
    }

    jwtAlgorithm match {
      case rsa: JwtRSAAlgorithm     => loadRSAKeys(loadPrivate[RSAPrivateKey], loadPublic[RSAPublicKey], rsa)
      case ecdsa: JwtECDSAAlgorithm => loadECKeys(loadPrivate[ECPrivateKey], loadPublic[ECPublicKey], ecdsa)
    }
  }

  override def loadPublicKeyFromFile(jwtAlgorithm: JwtAsymmetricAlgorithm, publicKeyFilePath: String): IO[PublicKey] = {
    def loadPublic[A]: PublicLoaderFunc[A] =
      errorIfMissingOrLoadUsing("publicKeyFilePath")(publicKeyFilePath)(_)

    jwtAlgorithm match {
      case _: JwtRSAAlgorithm   => loadPublic(RSAKeyLoader.loadPublicKey)
      case _: JwtECDSAAlgorithm => loadPublic(ECKeyLoader.loadPublicKey)
    }
  }

  override def loadKeysFromConfig(config: OAuth2KeysConfig): IO[AlgorithmWithKeys] =
    loadKeysFromFiles(
      config.jwtAlgorithm,
      config.publicKeyFilePath,
      config.privateKeyFilePath,
      config.privateKeyPassword
    )

  override def loadPublicKeyFromConfig(config: JwtConfig): IO[PublicKey] =
    loadPublicKeyFromFile(config.jwtAlgorithm, config.publicKeyFilePath)

  private def loadRSAKeys(
    privateKeyLoader: PrivateLoaderFunc[RSAPrivateKey],
    publicKeyLoader: PublicLoaderFunc[RSAPublicKey],
    algorithm: JwtRSAAlgorithm
  ): IO[RSAKeyPair] = {
    for {
      publicKey  <- publicKeyLoader(RSAKeyLoader.loadPublicKey)
      privateKey <- privateKeyLoader(RSAKeyLoader.loadPrivateKey)
    } yield RSAKeyPair(publicKey, privateKey, algorithm)
  }

  private def loadECKeys(
    privateKeyLoader: PrivateLoaderFunc[ECPrivateKey],
    publicKeyLoader: PublicLoaderFunc[ECPublicKey],
    algorithm: JwtECDSAAlgorithm
  ): IO[ECKeyPair] =
    for {
      publicKey  <- publicKeyLoader(ECKeyLoader.loadPublicKey)
      privateKey <- privateKeyLoader(ECKeyLoader.loadPrivateKey)
    } yield ECKeyPair(publicKey, privateKey, algorithm)

  private def errorIfMissingOrLoadUsing[A, B](
    keyName: String
  )(pathText: String)(loader: String => Either[Throwable, A]): IO[A] =
    if (pathText.isEmpty) IO.raiseError(KeyLoader.KeyLoaderException(s"Missing $keyName path"))
    else IO.fromEither(loader(pathText))

}
