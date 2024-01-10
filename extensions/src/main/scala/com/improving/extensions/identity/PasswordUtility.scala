package com.improving.extensions.identity

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PasswordUtility {
  final def apply(settings: PepperingSettings): PasswordUtility = new PasswordUtility(settings, new SecureRandom)

  final case class Result(salt: Array[Byte], hashedPassword: Array[Byte])

  private object Argon2Params {
    final val hashLength = 32

    // DO NOT change these unless you know what you are doing. They were sourced directly from OWASP, here:
    // https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#argon2id
    final private val iterations  = 2
    final private val memLimit    = 19456
    final private val parallelism = 1

    def usingSalt(salt: Array[Byte]) =
      new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
        .withVersion(Argon2Parameters.ARGON2_VERSION_13)
        .withIterations(iterations)
        .withMemoryAsKB(memLimit)
        .withParallelism(parallelism)
        .withSalt(salt)
        .build()

  }

}

final class PasswordUtility private (settings: PepperingSettings, secureRNG: java.util.Random) {
  import PasswordUtility._

  private type ByteArray = Array[Byte]

  def hashForStorage(plainTextPassword: String): Result = {
    val uniqueSalt = generateNewSalt()
    val peppering  = createPeppering()
    val redText    = plainTextPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8)
    val blackText  = hashPasswordInternal(uniqueSalt, redText, peppering)

    Result(uniqueSalt, blackText)
  }

  def verify(plainTextPassword: String, salt: Array[Byte], hashedPassword: Array[Byte]): Boolean = {
    val peppering = createPeppering()
    val redText   = plainTextPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8)
    val blackText = hashPasswordInternal(salt, redText, peppering)

    java.util.Arrays.equals(blackText, hashedPassword)
  }

  private def createPeppering(): Option[Mac] =
    settings match {
      case PepperingSettings.Disabled                          => None
      case enabled @ PepperingSettings.Enabled(algorithm, key) =>
        val secretKey = new SecretKeySpec(key.rawBytes, algorithm)
        val mac       = enabled.tryGetMac.fold(throw _, identity)
        mac.init(secretKey)
        Some(mac)
    }

  private def hashPasswordInternal(salt: ByteArray, redText: ByteArray, peppering: Option[Mac]): ByteArray = {
    assert(salt.nonEmpty)
    assert(redText.nonEmpty)

    // Create the Argon2Id parameters
    val argon2Params = Argon2Params.usingSalt(salt)

    // Hash the password
    val generator = new Argon2BytesGenerator
    generator.init(argon2Params)
    val result    = new Array[Byte](Argon2Params.hashLength)
    generator.generateBytes(redText, result, 0, result.length)

    // If we are not peppering, return the result, otherwise pepper the result
    peppering match {
      case None      => result
      case Some(mac) => mac.doFinal(result)
    }
  }

  private def generateNewSalt(): Array[Byte] = {
    val salt = Array.ofDim[Byte](16)
    secureRNG.nextBytes(salt)
    salt
  }

}
