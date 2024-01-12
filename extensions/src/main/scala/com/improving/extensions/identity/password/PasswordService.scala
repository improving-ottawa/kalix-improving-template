package com.improving.extensions.identity.password

import com.improving.extensions.identity.crypto.HMAC
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

import java.security.SecureRandom

object PasswordService {
  final def apply(settings: PepperingSettings): PasswordService = new PasswordService(settings)

  final def apply(): PasswordService = new PasswordService(PepperingSettings.Disabled)

  final case class Result(salt: Array[Byte], hashedPassword: Array[Byte])

  /* Internal Stuff */

  private type ByteArray = Array[Byte]

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

/** @note  */
final class PasswordService private(settings: PepperingSettings) {
  import PasswordService._

  private val secureRNG = new SecureRandom
  private val generator = new Argon2BytesGenerator
  private val peppering = createPeppering()

  def hashForStorage(plainTextPassword: String): Result = {
    val uniqueSalt = generateNewSalt()
    val redText    = plainTextPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8)
    val blackText  = hashPasswordInternal(uniqueSalt, redText, peppering)

    Result(uniqueSalt, blackText)
  }

  def verify(plainTextPassword: String, salt: Array[Byte], hashedPassword: Array[Byte]): Boolean = {
    val redText   = plainTextPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8)
    val blackText = hashPasswordInternal(salt, redText, peppering)

    java.util.Arrays.equals(blackText, hashedPassword)
  }

  private def createPeppering(): Option[HMAC] =
    settings match {
      case PepperingSettings.Disabled         => None
      case enabled: PepperingSettings.Enabled => enabled.tryGetMac.toOption
    }

  private def hashPasswordInternal(salt: ByteArray, redText: ByteArray, peppering: Option[HMAC]): ByteArray = {
    assert(salt.nonEmpty)
    assert(redText.nonEmpty)

    // Create the Argon2Id parameters
    val argon2Params = Argon2Params.usingSalt(salt)

    // The output result array
    val result = new Array[Byte](Argon2Params.hashLength)

    // Hash the password
    generator.init(argon2Params)
    generator.generateBytes(redText, result, 0, result.length)

    // If we are not peppering, return the result, otherwise pepper the result
    peppering match {
      case None      => result
      case Some(mac) => mac.hash(result)
    }
  }

  private def generateNewSalt(): Array[Byte] = {
    val salt = Array.ofDim[Byte](16)
    secureRNG.nextBytes(salt)
    salt
  }

}
