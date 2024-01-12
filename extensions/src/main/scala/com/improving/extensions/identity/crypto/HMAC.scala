package com.improving.extensions.identity.crypto

import com.improving.utils.SecureString

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import scala.util.Try

/**
 * This class provides the functionality of a "Hash-based Message Authentication Code" (HMAC) algorithm.
 *
 * @note This class is __absolutely not__ thread-safe!
 */
final class HMAC private(instance: Mac) {

  def hash(data: Array[Byte]): Array[Byte] = {
    instance.reset()
    instance.doFinal(data)
  }

}

object HMAC {

  // Load the BouncyCastle provider on class instantiation
  com.improving.utils.BouncyCastle.register()

  def create(privateKey: SecureString, hmacAlgorithmName: String): Try[HMAC] =
    Try(createUnsafe(privateKey, hmacAlgorithmName))

  def createUnsafe(privateKey: SecureString, hmacAlgorithmName: String): HMAC = {
    val secretKey = new SecretKeySpec(privateKey.readOnce(), hmacAlgorithmName)
    val macInstance = Mac.getInstance(hmacAlgorithmName)
    macInstance.init(secretKey)

    new HMAC(macInstance)
  }

}
