package com.example.utils.iam.cryptography

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter

import java.io.{File, FileNotFoundException}
import java.nio.file.{Path, Paths}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{PrivateKey, Provider, PublicKey, Security}
import scala.util.Try

abstract class KeyLoaderBase {
  // Key types
  protected type PubKey <: PublicKey
  protected type PrivKey <: PrivateKey

  // Crypto stuff
  final protected val bcProvider: Provider = new BouncyCastleProvider
  final protected val converter            = new JcaPEMKeyConverter().setProvider(bcProvider)

  // Load the BouncyCastle provider on class instantiation
  Security.addProvider(bcProvider)

  // Impl types
  sealed protected trait Password { def toCharArray: Array[Char] }

  protected object Password {
    def apply(textOption: Option[String]): Password = textOption.fold[Password](NoPassword)(PasswordProvided)
  }

  final protected case class PasswordProvided(clearText: String) extends Password {
    def toCharArray: Array[Char] = clearText.toCharArray
  }

  final protected case object NoPassword extends Password { def toCharArray: Array[Char] = Array.empty[Char] }

  /* KeyLoader Methods */
  def loadPrivateKey(path: Path, passwordOpt: Option[String]): Either[Throwable, PrivKey] =
    privateKeyLoader(getFileFromPath(path), Password(passwordOpt))

  def loadPrivateKey(path: String, passwordOpt: Option[String]): Either[Throwable, PrivKey] =
    privateKeyLoader(getFileFromText(path), Password(passwordOpt))

  def loadPrivateKey(file: File, passwordOpt: Option[String]): Either[Throwable, PrivKey] =
    privateKeyLoader(Right(file), Password(passwordOpt))

  def loadPublicKey(path: Path): Either[Throwable, PubKey]   = publicKeyLoader(getFileFromPath(path))
  def loadPublicKey(path: String): Either[Throwable, PubKey] = publicKeyLoader(getFileFromText(path))
  def loadPublicKey(file: File): Either[Throwable, PubKey]   = publicKeyLoader(Right(file))

  /* Crypto implementation methods */
  @inline private def privateKeyLoader(fileEither: Either[Throwable, File], password: Password) =
    keyLoader(fileEither, password)(privateKeyReader)(createPrivateKey)

  @inline private def publicKeyLoader(fileEither: Either[Throwable, File]) =
    keyLoader(fileEither, NoPassword)(publicKeyReader)(createPublicKey)

  @inline private def publicKeyReader(file: File, password: Password) = readFilePublicKey(file, getFileExtension(file))

  @inline private def privateKeyReader(file: File, password: Password) =
    readFilePrivateKey(file, getFileExtension(file), password)

  protected def keyLoader[A, K](fileEither: Either[Throwable, File], password: Password)(
    keyReader: (File, Password) => Either[Throwable, K]
  )(loaderF: K => Either[Throwable, A]): Either[Throwable, A] =
    for {
      file      <- fileEither
      foundFile <- checkFileExits(file)
      keyPair   <- keyReader(foundFile, password)
      outKey    <- loaderF(keyPair)
    } yield outKey

  /* Abstract methods */
  protected def readFilePrivateKey(
    file: File,
    extension: String,
    password: Password
  ): Either[Throwable, PKCS8EncodedKeySpec]

  protected def readFilePublicKey(file: File, extension: String): Either[Throwable, X509EncodedKeySpec]
  protected def createPrivateKey(keySpec: PKCS8EncodedKeySpec): Either[Throwable, PrivKey]
  protected def createPublicKey(keySpec: X509EncodedKeySpec): Either[Throwable, PubKey]

  // Convenience / Helper methods
  final protected def getFileFromPath(path: Path)   = Try(path.toFile).toEither
  final protected def getFileFromText(path: String) = Try(Paths.get(path).toFile).toEither

  final protected def checkFileExits(file: File) =
    if (file.exists) Right(file) else Left(new FileNotFoundException(file.getAbsolutePath))

  final protected def getFileExtension(file: File) = file.getName.split('.').last.toLowerCase
}
