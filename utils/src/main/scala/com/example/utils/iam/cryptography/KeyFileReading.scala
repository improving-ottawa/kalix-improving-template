package com.example.utils.iam.cryptography

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo
import org.bouncycastle.openssl.jcajce.{JceOpenSSLPKCS8DecryptorProviderBuilder, JcePEMDecryptorProviderBuilder}
import org.bouncycastle.openssl.{PEMEncryptedKeyPair, PEMKeyPair, PEMParser}
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
import org.bouncycastle.util.io.pem.PemReader

import java.io.{File, FileInputStream, InputStreamReader, Reader}
import java.nio.file.Files
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

trait KeyFileReading { self: KeyLoaderBase =>

  /*
   * KeyLoader overrides
   */
  override protected def readFilePrivateKey(file: File, extension: String, password: Password) =
    extension.toLowerCase match {
      case "pem" => readPEMPrivateKey(file, password)
      case "der" => readDERPrivateKey(file, password)
      case other => Left(new UnsupportedOperationException(s"Cannot read keys from file with extension: $other"))
    }

  override protected def readFilePublicKey(file: File, extension: String) =
    extension.toLowerCase match {
      case "pem" => readPEMPublicKey(file)
      case "der" => readDERPublicKey(file)
      case other => Left(new UnsupportedOperationException(s"Cannot read keys from file with extension: $other"))
    }

  /*
   * Read public/private keys from specific file types
   */
  final protected def readDERPrivateKey(file: File, password: Password): Either[Throwable, PKCS8EncodedKeySpec] =
    readFileBytesThen(file)(extractDERPrivateKey(_, password)).attempt.unsafeRunSync()

  final protected def readDERPublicKey(file: File): Either[Throwable, X509EncodedKeySpec] =
    readFileBytesThen(file)(extractDERPublicKey).attempt.unsafeRunSync()

  final protected def readPEMPrivateKey(file: File, password: Password): Either[Throwable, PKCS8EncodedKeySpec] =
    fileReaderThen(file)(extractPEMPrivateKey(_, password)).attempt.unsafeRunSync()

  final protected def readPEMPublicKey(file: File): Either[Throwable, X509EncodedKeySpec] =
    fileReaderThen(file)(extractPEMPublicKey).attempt.unsafeRunSync()

  /*
   * Implementation
   */
  final private def readFileBytesThen[A](file: File)(thenF: Array[Byte] => IO[A]): IO[A] =
    IO(Files.readAllBytes(file.toPath)).flatMap(thenF)

  final private def fileReaderThen[A](file: File)(thenF: Reader => IO[A]): IO[A] =
    IO(new InputStreamReader(new FileInputStream(file))).bracket { reader => thenF(reader) } { reader =>
      IO(reader.close())
    }

  final private def extractDERPrivateKey(fileBytes: Array[Byte], password: Password) = {
    // Assume encrypted private key, fallback with unencrypted private key
    IO {
      val encKeyInfo  = EncryptedPrivateKeyInfo.getInstance(fileBytes)
      val encObj      = new PKCS8EncryptedPrivateKeyInfo(encKeyInfo)
      val decryptProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password.toCharArray)
      val keyInfo     = encObj.decryptPrivateKeyInfo(decryptProv)
      val privKey     = converter.getPrivateKey(keyInfo)
      new PKCS8EncodedKeySpec(privKey.getEncoded)
    }.handleErrorWith { _ =>
      IO(new PKCS8EncodedKeySpec(fileBytes))
    }
  }

  final private def extractDERPublicKey(fileBytes: Array[Byte]) = IO(new X509EncodedKeySpec(fileBytes))

  final private def extractPEMPrivateKey(reader: Reader, password: Password) = {
    IO(new PEMParser(reader)).bracket { pemParser => IO(handlePEMObject(pemParser.readObject, password)) } {
      pemParser => IO(pemParser.close())
    }
  }

  final private def extractPEMPublicKey(reader: Reader) =
    IO(new PemReader(reader)).bracket { pemReader =>
      IO(new X509EncodedKeySpec(pemReader.readPemObject().getContent))
    } { pemReader => IO(pemReader.close()) }

  final private def handlePEMObject(pemObject: Object, password: Password) = {
    @inline def readEncrypted(ckp: PEMEncryptedKeyPair) = {
      val decryptor = new JcePEMDecryptorProviderBuilder().build(password.toCharArray)
      val keyPair   = converter.getKeyPair(ckp.decryptKeyPair(decryptor))
      new PKCS8EncodedKeySpec(keyPair.getPrivate.getEncoded)
    }

    @inline def readUnencrypted(kp: PEMKeyPair) = {
      val keyPair = converter.getKeyPair(kp)
      new PKCS8EncodedKeySpec(keyPair.getPrivate.getEncoded)
    }

    pemObject match {
      case ekp: PEMEncryptedKeyPair => readEncrypted(ekp)
      case kp: PEMKeyPair           => readUnencrypted(kp)
    }
  }

}
