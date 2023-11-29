package com.improving.utils.iam

import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce._
import org.bouncycastle.openssl._
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
import org.bouncycastle.util.io.pem.PemReader

import scala.util._

import java.io._
import java.nio.file.{Files, Paths}
import java.security.{Security, Provider}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

private object KeyLoaderImpl {
  /* Cryptographic stuff (BouncyCastle) */
  final private type Password = Option[String]
  final private val bcProvider = new BouncyCastleProvider

  // Load the BouncyCastle provider on class instantiation
  Security.addProvider(bcProvider)
}

abstract class KeyLoaderImpl { self: KeyLoader[_ <: AlgorithmWithKeys] =>
  import KeyLoaderImpl._

  /* Cryptographic stuff (BouncyCastle) */
  final private val converter = new JcaPEMKeyConverter().setProvider(bcProvider)
  final protected def cryptoProvider: Provider = bcProvider

  /*
   * KeyLoader overrides
   */
  protected def readFilePrivateKey(filePath: String, password: Password): Either[Throwable, PKCS8EncodedKeySpec] =
    for {
      file    <- getFileFromText(filePath)
      _       <- checkFileExits(file)
      keySpec <- getFileExtension(file) match {
        case "pem" => readPEMPrivateKey(file, password)
        case "der" => readDERPrivateKey(file, password)
        case other => Left(new UnsupportedOperationException(s"Cannot read keys from file with extension: $other"))
      }
    } yield keySpec

  protected def readFilePublicKey(filePath: String): Either[Throwable, X509EncodedKeySpec] =
    for {
      file    <- getFileFromText(filePath)
      _       <- checkFileExits(file)
      keySpec <- getFileExtension(file) match {
        case "pem" => readPEMPublicKey(file)
        case "der" => readDERPublicKey(file)
        case other => Left(new UnsupportedOperationException(s"Cannot read keys from file with extension: $other"))
      }
    } yield keySpec

  /*
   * Read public/private keys from specific file types
   */
  final protected def readDERPrivateKey(file: File, password: Password): Either[Throwable, PKCS8EncodedKeySpec] =
    readFileBytesThen(file)(extractDERPrivateKey(_, password)).toEither

  final protected def readDERPublicKey(file: File): Either[Throwable, X509EncodedKeySpec] =
    readFileBytesThen(file)(extractDERPublicKey).toEither

  final protected def readPEMPrivateKey(file: File, password: Password): Either[Throwable, PKCS8EncodedKeySpec] =
    fileReaderThen(file)(extractPEMPrivateKey(_, password)).toEither

  final protected def readPEMPublicKey(file: File): Either[Throwable, X509EncodedKeySpec] =
    fileReaderThen(file)(extractPEMPublicKey).toEither

  /*
   * Implementation
   */
  final private def readFileBytesThen[A](file: File)(thenF: Array[Byte] => Try[A]): Try[A] =
    Try(Files.readAllBytes(file.toPath)).flatMap(thenF)

  final private def fileReaderThen[A](file: File)(thenF: Reader => Try[A]): Try[A] =
    usingResource(new InputStreamReader(new FileInputStream(file)))(thenF)

  final private def extractDERPrivateKey(fileBytes: Array[Byte], password: Password) =
    Try {
      // If a password is provided, assume encrypted private key
      password match {
        case Some(plainText) =>
          val encKeyInfo = EncryptedPrivateKeyInfo.getInstance(fileBytes)
          val encObj = new PKCS8EncryptedPrivateKeyInfo(encKeyInfo)
          val decryptProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(plainText.toCharArray)
          val keyInfo = encObj.decryptPrivateKeyInfo(decryptProv)
          val privKey = converter.getPrivateKey(keyInfo)
          new PKCS8EncodedKeySpec(privKey.getEncoded)

        case None =>
          new PKCS8EncodedKeySpec(fileBytes)
      }
  }

  final private def extractDERPublicKey(fileBytes: Array[Byte]) =
    Try(new X509EncodedKeySpec(fileBytes))

  final private def extractPEMPrivateKey(reader: Reader, password: Password) =
    usingResource(new PEMParser(reader))(pemParser => Try(handlePEMObject(pemParser.readObject, password)))

  final private def extractPEMPublicKey(reader: Reader) =
    usingResource(new PemReader(reader))(pemReader =>
      Try(new X509EncodedKeySpec(pemReader.readPemObject().getContent))
    )

  final private def handlePEMObject(pemObject: Object, password: Password) = {
    @inline def readEncrypted(ckp: PEMEncryptedKeyPair, plainTextPassword: String) = {
      val decryptor = new JcePEMDecryptorProviderBuilder().build(plainTextPassword.toCharArray)
      val keyPair = converter.getKeyPair(ckp.decryptKeyPair(decryptor))
      new PKCS8EncodedKeySpec(keyPair.getPrivate.getEncoded)
    }

    @inline def readUnencrypted(kp: PEMKeyPair) = {
      val keyPair = converter.getKeyPair(kp)
      new PKCS8EncodedKeySpec(keyPair.getPrivate.getEncoded)
    }

    lazy val encryptedNoPassword = "Tried to load an encrypted PEM object with no password provided."
    lazy val unencryptedWithPassword = "Tried to load an unencrypted PEM object with a password provided."

    (pemObject, password) match {
      case (ekp: PEMEncryptedKeyPair, Some(plainText))  => readEncrypted(ekp, plainText)
      case (_: PEMEncryptedKeyPair, None)               => throw new RuntimeException(encryptedNoPassword)
      case (kp: PEMKeyPair, None)                       => readUnencrypted(kp)
      case (_: PEMEncryptedKeyPair, Some(_))            => throw new RuntimeException(unencryptedWithPassword)
    }
  }

  // Convenience / Helper methods

  final private def usingResource[R <: AutoCloseable, Out](resource: R)(func: R => Try[Out]): Try[Out] =
    func(resource).transform(
      res => Try(resource.close()).map(_ => res),
      err => Try(resource.close()).flatMap(_ => Failure(err))
    )

  final private def getFileFromText(path: String) =
    if (path.startsWith("resource:"))
      Try {
        val resourcePath = path.stripPrefix("resource:")
        val url = getClass.getResource(resourcePath)
        new java.io.File(url.toURI)
      }.toEither
    else Try(Paths.get(path).toFile).toEither

  final private def checkFileExits(file: File) =
    if (file.exists) Right(file) else Left(new FileNotFoundException(file.getAbsolutePath))

  final private def getFileExtension(file: File) = file.getName.split('.').last.toLowerCase

}
