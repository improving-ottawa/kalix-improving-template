package com.improving.extensions.identity.crypto

import com.improving.utils.SecureString
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.security.SecureRandom
import scala.util.Random

class HMACSpec extends AnyWordSpec with Matchers {

  private val secureRNG = new Random(new SecureRandom)

  private def randomData(length: Int): Array[Byte] = secureRNG.nextBytes(length)
  private def randomString(length: Int): String = secureRNG.nextString(length)

  val testedMACs = List(
    "Blake3-256",
    "HMac-KECCAK256",
    "HMac-Skein-512-256",
    "HMacSHA256",
    "SHA3-256"
  )

  /* ScalaTest */

  "HMAC" when {

    "using algorithm 'Blake3Mac'" should {
      "instantiate correctly and produce hashes" in testHMacAlgorithm(testedMACs.head)
    }

    "using algorithm 'KECCAK256'" should {
      "instantiate correctly and produce hashes" in testHMacAlgorithm(testedMACs(1))
    }

    "using algorithm 'SkeinMAC256'" should {
      "instantiate correctly and produce hashes" in testHMacAlgorithm(testedMACs(2))
    }

    "using algorithm 'HMacSHA256'" should {
      "instantiate correctly and produce hashes" in testHMacAlgorithm(testedMACs(3))
    }

    "using algorithm 'HMacSHA3-256'" should {
      "instantiate correctly and produce hashes" in testHMacAlgorithm(testedMACs(4))
    }
  }

  /* Specifications */

  def testHMacAlgorithm(name: String) = {
    val testData = randomData(64 * 1024)  // 64K block

    val keyText = randomString(8 + secureRNG.nextInt(12))
    val privateKey = SecureString(keyText)
    val hmac = HMAC.createUnsafe(privateKey, name)

    val hash1 = hmac.hash(testData)
    hash1.length must be > 0

    val keyText2 = {
      val chars = keyText.toCharArray
      chars(0) = (~chars(0).toInt).toChar  // bit-flip the first character
      new String(chars)
    }

    val privateKey2 = SecureString(keyText2)
    val hmac2 = HMAC.createUnsafe(privateKey2, name)

    val hash2 = hmac2.hash(testData)

    hash1.length mustBe hash2.length
    java.util.Arrays.equals(hash1, hash2) mustBe false
  }

}
