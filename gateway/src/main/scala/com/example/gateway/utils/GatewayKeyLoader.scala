package com.example.gateway.utils

import com.typesafe.config.ConfigFactory

import com.example.utils.KeyLoader
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{KeyFactory, PrivateKey, PublicKey}
import javax.crypto.spec.PBEKeySpec
import javax.crypto.{Cipher, EncryptedPrivateKeyInfo, SecretKeyFactory}

object GatewayKeyLoader extends KeyLoader {

  protected def loadPrivateKey(): PrivateKey = {
    val passwordOption      = privateKeyPassword
    val (privateKeyPath, _) = keyFilePaths(passwordOption.isDefined)
    val privateKeyBytes     = loadPrivateKeyFromResource(privateKeyPath)

    passwordOption match {
      case Some(password) => readEncryptedPrivateKey(privateKeyBytes, password)
      case None           => readUnencryptedPrivateKey(privateKeyBytes)
    }
  }

  protected def loadPublicKey(): PublicKey = {
    val passwordOption     = privateKeyPassword
    val (_, publicKeyPath) = keyFilePaths(passwordOption.isDefined)
    val publicKeyBytes     = loadPublicKeyFromResource(publicKeyPath)
    readPublicKey(publicKeyBytes)
  }

  private def keyFilePaths(hasPassword: Boolean): (String, String) =
    if (hasPassword) {
      ("keys/jwt-key.private", "keys/jwt-key.der")
    } else {
      ("keys/test-key.private", "keys/test-key.der")
    }

  private def privateKeyPassword: Option[String] = {
    val config = ConfigFactory.load()
    if (config.hasPath("com.ott.gateway.jwt.private-key-password")) {
      val privateKey = config.getString("com.ott.gateway.jwt.private-key-password")
      if (privateKey.isBlank) None else Some(privateKey)
    } else None
  }

  private def loadPublicKeyFromResource(resourceName: String) = {
    val source = Thread.currentThread().getContextClassLoader.getResourceAsStream(resourceName)
    val reader = new java.io.DataInputStream(source)
    try {
      reader.readAllBytes()
    } finally {
      reader.close()
      source.close()
    }
  }

  private def loadPrivateKeyFromResource(resourceName: String) = {
    val decoder = java.util.Base64.getDecoder
    val reader  = scala.io.Source.fromResource(resourceName)
    try {
      val fileText = reader.getLines().mkString(System.lineSeparator)
      decoder.decode(
        fileText
          .replaceAll("-----BEGIN (.*)-----", "")
          .replaceAll("-----END (.*)-----", "")
          .replaceAll("\r\n", "")
          .replaceAll("\n", "")
          .trim
      )
    } finally {
      reader.close()
    }
  }

  private def readEncryptedPrivateKey(keyBytes: Array[Byte], password: String) = {
    val encryptedPKInfo = new EncryptedPrivateKeyInfo(keyBytes)
    val algName         = "PBEWithHmacSHA256AndAES_128"
    val cipher          = Cipher.getInstance(algName)
    val pbeKeySpec      = new PBEKeySpec(password.toCharArray)
    val secretKeyFac    = SecretKeyFactory.getInstance(algName)
    val pbeKey          = secretKeyFac.generateSecret(pbeKeySpec)

    cipher.init(Cipher.DECRYPT_MODE, pbeKey, encryptedPKInfo.getAlgParameters)
    val pkcs8KeySpec = encryptedPKInfo.getKeySpec(cipher)
    val keyFactory   = KeyFactory.getInstance("RSA")
    keyFactory.generatePrivate(pkcs8KeySpec)
  }

  private def readUnencryptedPrivateKey(keyBytes: Array[Byte]) =
    KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes))

  private def readPublicKey(keyBytes: Array[Byte]) =
    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes))

}
