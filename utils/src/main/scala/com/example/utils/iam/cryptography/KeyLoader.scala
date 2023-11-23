package com.example.utils.iam.cryptography

import java.io.File
import java.nio.file.Path
import java.security._
import java.security.interfaces.{ECPrivateKey, ECPublicKey, RSAPrivateKey, RSAPublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import cats.syntax.either._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.chatwork.scala.jwk.JWKError.PublicKeyCreationError
import com.chatwork.scala.jwk.{AssymetricJWK, ECJWK, JWK, RSAJWK}

import scala.util.{Success, Try}

/**
  * A cryptographic key loader, which can read public/private keys from input files.
  *
  * @tparam Pub
  *   The type of public key loaded by this key loader.
  * @tparam Priv
  *   The type of private key loaded by this key loader.
  * @tparam JWKType
  *   The type of JWK that can be loaded by this key loader.
  */
sealed trait KeyLoader[Pub <: PublicKey, Priv <: PrivateKey, JWKType <: JWK with AssymetricJWK] {
  import KeyLoader._

  final protected type PubKey  = Pub
  final protected type PrivKey = Priv

  @volatile private[this] var state: State = Empty

  final def privateKey: Try[PrivateKey] =
    state match {
      case Cached(privateKey, _) => Success(privateKey)
      case Empty                 => loadAndCacheKeys.map(_.privateKey)
    }

  final def publicKey: Try[PublicKey] =
    state match {
      case Cached(_, publicKey) => Success(publicKey)
      case Empty                => loadAndCacheKeys.map(_.publicKey)
    }

  def loadPrivateKey(path: Path, passwordOption: Option[String]): Either[Throwable, PrivKey]
  def loadPrivateKey(path: String, passwordOption: Option[String]): Either[Throwable, PrivKey]
  def loadPrivateKey(file: File, passwordOption: Option[String]): Either[Throwable, PrivKey]

  def loadPublicKey(path: Path): Either[Throwable, PubKey]
  def loadPublicKey(path: String): Either[Throwable, PubKey]
  def loadPublicKey(file: File): Either[Throwable, PubKey]

  def loadPublicKeyFromJWK(jwk: JWKType): Either[Throwable, PubKey]

  private def loadAndCacheKeys: Try[Cached] =
    synchronized {
      state match {
        case cached @ Cached(_, _) => Success(cached)
        case Empty                 =>
          Try {
            val privateKey = loadPrivateKey()
            val publicKey  = loadPublicKey()
            val cached     = (privateKey, publicKey) match {
              case (Right(priv), Right(pub)) => Cached(priv, pub)
            }
            state = cached
            cached
          }
      }
    }

}

object KeyLoader {
  sealed private trait State
  private case object Empty                                               extends State
  private case class Cached(privateKey: PrivateKey, publicKey: PublicKey) extends State

  final type AsymmetricJWK    = JWK with AssymetricJWK
  final type GenericKeyLoader = KeyLoader[PublicKey, PrivateKey, AsymmetricJWK]
  final type RSAKeyLoader     = KeyLoader[RSAPublicKey, RSAPrivateKey, RSAJWK]
  final type ECKeyLoader      = KeyLoader[ECPublicKey, ECPrivateKey, ECJWK]

  // Generic key loader exception
  final case class KeyLoaderException(message: String, cause: Option[Throwable] = None)
      extends Exception(message, cause.orNull)

  /**
    * RSA Public/Private KeyLoader
    */
  final val rsaKeyLoader: RSAKeyLoader = RSAKeyLoader

  /**
    * Elliptic Curve Public/Private KeyLoader Implementation
    */
  final val ecKeyLoader: ECKeyLoader = ECKeyLoader

  /**
    * Asymmetric JWK Public key loader
    */
  final def fromAsymmetricJwk(jwk: AsymmetricJWK): Either[Throwable, PublicKey] =
    jwk match {
      case rsa: RSAJWK => RSAKeyLoader.loadPublicKeyFromJWK(rsa)
      case ec: ECJWK   => ECKeyLoader.loadPublicKeyFromJWK(ec)
      case other       => Left(KeyLoaderException(s"Unknown JWK type: $other"))
    }

}

/* KeyLoader Instances */

/**
  * RSA Public/Private KeyLoader Implementation
  */
object RSAKeyLoader extends KeyLoaderBase with KeyLoader[RSAPublicKey, RSAPrivateKey, RSAJWK] with KeyFileReading {
  final private val keyFactory = KeyFactory.getInstance("RSA", bcProvider)

  override protected def createPrivateKey(keySpec: PKCS8EncodedKeySpec): Either[Throwable, RSAPrivateKey] =
    IO(keyFactory.generatePrivate(keySpec)).map(_.asInstanceOf[RSAPrivateKey]).attempt.unsafeRunSync()

  override protected def createPublicKey(keySpec: X509EncodedKeySpec): Either[Throwable, RSAPublicKey] =
    IO(keyFactory.generatePublic(keySpec)).map(_.asInstanceOf[RSAPublicKey]).attempt.unsafeRunSync()

  override def loadPublicKeyFromJWK(jwk: RSAJWK): Either[Throwable, RSAPublicKey] = {
    def handleJwkError(error: PublicKeyCreationError) = KeyLoader.KeyLoaderException(error.message)
    jwk.toRSAPublicKey.leftMap(handleJwkError)
  }

}

/**
  * Elliptic Curve Public/Private KeyLoader Implementation
  */
object ECKeyLoader extends KeyLoaderBase with KeyLoader[ECPublicKey, ECPrivateKey, ECJWK] with KeyFileReading {
  final private val keyFactory = KeyFactory.getInstance("ECDSA", bcProvider)

  override protected def createPrivateKey(keySpec: PKCS8EncodedKeySpec): Either[Throwable, ECPrivateKey] =
    IO(keyFactory.generatePrivate(keySpec)).map(_.asInstanceOf[ECPrivateKey]).attempt.unsafeRunSync()

  override protected def createPublicKey(keySpec: X509EncodedKeySpec): Either[Throwable, ECPublicKey] =
    IO(keyFactory.generatePublic(keySpec)).map(_.asInstanceOf[ECPublicKey]).attempt.unsafeRunSync()

  override def loadPublicKeyFromJWK(jwk: ECJWK): Either[Throwable, ECPublicKey] = {
    def handleJwkError(error: PublicKeyCreationError) = KeyLoader.KeyLoaderException(error.message)
    jwk.toECPublicKey(Some(bcProvider)).leftMap(handleJwkError)
  }

}
