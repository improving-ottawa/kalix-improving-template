package com.improving.iam

import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce._
import org.bouncycastle.openssl._
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
import org.bouncycastle.util.io.pem.PemReader

import scala.util._

import java.io._
import java.nio.file.Paths
import java.security.{Provider, Security}
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
      stream  <- getFileStreamFromPath(filePath)
      keySpec <- getFileExtension(filePath) match {
                   case "pem" => readPEMPrivateKey(stream, password)
                   case "der" => readDERPrivateKey(stream, password)
                   case other =>
                     Left(new UnsupportedOperationException(s"Cannot read keys from file with extension: $other"))
                 }
    } yield keySpec

  protected def readFilePublicKey(filePath: String): Either[Throwable, X509EncodedKeySpec] =
    for {
      stream  <- getFileStreamFromPath(filePath)
      keySpec <- getFileExtension(filePath) match {
                   case "pem" => readPEMPublicKey(stream)
                   case "der" => readDERPublicKey(stream)
                   case other =>
                     Left(new UnsupportedOperationException(s"Cannot read keys from file with extension: $other"))
                 }
    } yield keySpec

  /*
   * Read public/private keys from specific file types
   */
  final protected def readDERPrivateKey(
    source: InputStream,
    password: Password
  ): Either[Throwable, PKCS8EncodedKeySpec] =
    readStreamBytesThen(source)(extractDERPrivateKey(_, password)).toEither

  final protected def readDERPublicKey(source: InputStream): Either[Throwable, X509EncodedKeySpec] =
    readStreamBytesThen(source)(extractDERPublicKey).toEither

  final protected def readPEMPrivateKey(
    source: InputStream,
    password: Password
  ): Either[Throwable, PKCS8EncodedKeySpec] =
    streamReaderThen(source)(extractPEMPrivateKey(_, password)).toEither

  final protected def readPEMPublicKey(source: InputStream): Either[Throwable, X509EncodedKeySpec] =
    streamReaderThen(source)(extractPEMPublicKey).toEither

  /*
   * Implementation
   */

  final private def readStreamBytesThen[A](source: InputStream)(thenF: Array[Byte] => Try[A]): Try[A] =
    usingResource(new ByteArrayOutputStream()) { baos =>
      usingResource(source) { inputStream =>
        val readFileTry = Try {
          val buffer = new Array[Byte](4096)
          var read   = inputStream.read(buffer)
          while (read != 0) {
            baos.write(buffer, 0, read)
            read = inputStream.read(buffer)
          }

          baos.toByteArray
        }

        readFileTry.flatMap(thenF)
      }
    }

  final private def streamReaderThen[A](stream: InputStream)(thenF: Reader => Try[A]): Try[A] =
    usingResource(stream)(inputStream => usingResource(new InputStreamReader(inputStream))(thenF))

  final private def extractDERPrivateKey(fileBytes: Array[Byte], password: Password) =
    Try {
      // If a password is provided, assume encrypted private key
      password match {
        case Some(plainText) =>
          val encKeyInfo  = EncryptedPrivateKeyInfo.getInstance(fileBytes)
          val encObj      = new PKCS8EncryptedPrivateKeyInfo(encKeyInfo)
          val decryptProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(plainText.toCharArray)
          val keyInfo     = encObj.decryptPrivateKeyInfo(decryptProv)
          val privKey     = converter.getPrivateKey(keyInfo)
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
    usingResource(new PemReader(reader))(pemReader => Try(new X509EncodedKeySpec(pemReader.readPemObject().getContent)))

  final private def handlePEMObject(pemObject: Object, password: Password) = {
    @inline def readEncrypted(ckp: PEMEncryptedKeyPair, plainTextPassword: String) = {
      val decryptor = new JcePEMDecryptorProviderBuilder().build(plainTextPassword.toCharArray)
      val keyPair   = converter.getKeyPair(ckp.decryptKeyPair(decryptor))
      new PKCS8EncodedKeySpec(keyPair.getPrivate.getEncoded)
    }

    @inline def readUnencrypted(kp: PEMKeyPair) = {
      val keyPair = converter.getKeyPair(kp)
      new PKCS8EncodedKeySpec(keyPair.getPrivate.getEncoded)
    }

    lazy val encryptedNoPassword     = "Tried to load an encrypted PEM object with no password provided."
    lazy val unencryptedWithPassword = "Tried to load an unencrypted PEM object with a password provided."

    @inline def failBecauseInvalid(): Nothing =
      throw new RuntimeException(
        "Error occurred while parsing PEM object. Expected either `PEMEncryptedKeyPair` or `PEMKeyPair` " +
          s"but got: ${pemObject.getClass.getName}"
      )

    (pemObject, password) match {
      case (ekp: PEMEncryptedKeyPair, Some(plainText)) => readEncrypted(ekp, plainText)
      case (_: PEMEncryptedKeyPair, None)              => throw new RuntimeException(encryptedNoPassword)
      case (kp: PEMKeyPair, None)                      => readUnencrypted(kp)
      case (_: PEMKeyPair, Some(_))                    => throw new RuntimeException(unencryptedWithPassword)
      case (_, _)                                      => failBecauseInvalid()
    }
  }

  // Convenience / Helper methods

  final private def usingResource[R <: AutoCloseable, Out](resource: R)(func: R => Try[Out]): Try[Out] =
    func(resource).transform(
      res => Try(resource.close()).map(_ => res),
      err => Try(resource.close()).flatMap(_ => Failure(err))
    )

  final private def getFileStreamFromPath(path: String) =
    if (path.startsWith("resource:"))
      Try {
        val resourcePath = path.stripPrefix("resource:")
        val classResPath = if (resourcePath.startsWith("/")) resourcePath else "/" + resourcePath

        Try(getClass.getResourceAsStream(classResPath))
          .recoverWith(error =>
            Failure(new RuntimeException(s"Could not load key from resource: $classResPath", error))
          )
      }.flatten.toEither
    else Try(new FileInputStream(Paths.get(path).toFile)).toEither

  final private def getFileExtension(filePath: String) = filePath.split('.').last.toLowerCase

}
