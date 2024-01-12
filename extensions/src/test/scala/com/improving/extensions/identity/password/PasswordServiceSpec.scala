package com.improving.extensions.identity.password

import com.improving.utils.SecureString

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PasswordServiceSpec extends AnyWordSpec with Matchers {
  import com.improving.extensions.identity.password.PasswordService.Result

  private val testStorage = scala.collection.mutable.Map.empty[String, Result]

  private val secureRandom = new scala.util.Random(new java.security.SecureRandom)

  private def randomString(minLen: Int = 3, maxLen: Int = 20): String =
    secureRandom.nextString(minLen + secureRandom.nextInt(maxLen - minLen))

  private def randomBytes(minLen: Int = 3, maxLen: Int = 20): Array[Byte] =
    randomString(minLen, maxLen).getBytes

  private val testPasswords = (1 to 128).map(_ => randomString()).toList

  private val testConfigurations = List(
    PepperingSettings.Disabled,
    PepperingSettings(SecureString(randomString()), "HmacSHA256"),
    PepperingSettings(SecureString(randomString()), "Blake3Mac")
  )

  private def addResult(plainText: String, result: Result): Unit = { testStorage.addOne((plainText, result)) }

  /* ScalaTest */

  "Password Utility" when {

    "using no peppering" should {
      testStorage.clear()

      "be able to hash passwords" in testHashing(PepperingSettings.Disabled, testPasswords)

      "not produce any hash collisions" in testCollisions()

      "be able to verify hashed passwords" in testVerification(PepperingSettings.Disabled)
    }

    "using HmacSHA256 peppering" should {
      testStorage.clear()
      val settings = testConfigurations(1)

      "be able to hash passwords" in testHashing(PepperingSettings.Disabled, testPasswords)

      "not produce any hash collisions" in testCollisions()

      "be able to verify hashed passwords" in testVerification(PepperingSettings.Disabled)
    }

    "using Blake3Mac peppering" should {
      testStorage.clear()
      val settings = testConfigurations(2)

      "be able to hash passwords" in testHashing(PepperingSettings.Disabled, testPasswords)

      "not produce any hash collisions" in testCollisions()

      "be able to verify hashed passwords" in testVerification(PepperingSettings.Disabled)
    }

  }

  /* Specifications */

  def testHashing(settings: PepperingSettings, passwords: List[String]) = {
    val utility = PasswordService(settings)
    for (password <- passwords) yield {
      val result = utility.hashForStorage(password)
      addResult(password, result)

      result.salt must not be empty
      result.hashedPassword must not be empty
    }
  }

  def testCollisions() = {
    val passwordHashes = testStorage.values.map(_.hashedPassword).toList
    for (hash <- passwordHashes)
      yield passwordHashes.count(array => java.util.Arrays.equals(hash, array)) mustBe 1
  }

  def testVerification(settings: PepperingSettings) = {
    val utility = PasswordService(settings)
    for ((plainText, result) <- testStorage.toList) yield {
      utility.verify(plainText, result.salt, result.hashedPassword) mustBe true
    }
  }

}
