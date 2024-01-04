package com.improving.utils

import com.improving.hashing.XXHash

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.security.SecureRandom
import java.util.UUID

class BloomFilterSpec extends AnyWordSpec with Matchers {

  "BloomFilter" should {

    "not produce false negatives" in {
      val errorRate = 0.01 // 1%
      val testItems = 100_000
      val secureRng = new SecureRandom()
      val bloom     = BloomFilter[Long](testItems, errorRate, (lng, seed) => XXHash.hashLong(lng, seed))

      // Fill the bloom filter with `testItems` longs from 1 to `testItems`
      val filledBloom = bloom.addAll((1 to testItems).map(_.toLong))

      // Check for any false negatives (should be zero)
      val falseNegatives = (1 to testItems).foldLeft(0)((acc, i) => if (filledBloom.contains(i)) acc else acc + 1)

      falseNegatives mustBe 0
    }

    "produce a correct error rate based on parameters" in {
      val errorRate = 0.05 // 5%
      val testItems = 1_000_000
      val secureRng = new SecureRandom()
      val bloom     = BloomFilter[Long](testItems, errorRate, (lng, seed) => XXHash.hashLong(lng, seed))

      // Fill the bloom filter with `testItems` longs from 1 to `testItems`
      val filledBloom = bloom.addAll((1 to testItems).map(_.toLong))

      val notContainedLongs =
        LazyList
          .continually(secureRng.nextLong())
          .filter(n => n > testItems || n < 1)
          .take(testItems)
          .toArray

      // Check the next 1,000,000 random longs
      val falsePositives =
        notContainedLongs.foldLeft(0L)((acc, n) => if (filledBloom.contains(n)) acc + 1 else acc)

      val actualErrorRate  = falsePositives / testItems.toDouble
      val errorRateCeiling = errorRate * 1.025 // Within 2.5%

      actualErrorRate must be < errorRateCeiling
      actualErrorRate must be > 0.0
    }

    "be serializable to/from a byte array (for storage)" in {
      val uuidHashFunc: BloomFilter.HashFunction[UUID] =
        (uuid, n) => XXHash.hashLongs(Array(uuid.getMostSignificantBits, uuid.getLeastSignificantBits), 1771 * n)

      val storedIds = (1 to 10000).map(_ => UUID.randomUUID).toList

      val (bloomFilter, notInId) = {
        val emptyFilter  = BloomFilter[UUID](250000, 0.1, uuidHashFunc)
        val filledFilter = emptyFilter.addAll(storedIds)

        val notInId = LazyList
          .continually(UUID.randomUUID)
          .filterNot(filledFilter.contains)
          .head

        (filledFilter, notInId)
      }

      bloomFilter.contains(notInId) mustBe false

      // Serialize
      val data = bloomFilter.toByteArray

      // Deserialize
      val recoveredFilter = BloomFilter.fromByteArray(data, uuidHashFunc)

      recoveredFilter.contains(notInId) mustBe false
      recoveredFilter.truthRatio mustBe bloomFilter.truthRatio
      recoveredFilter.numHashRounds mustBe bloomFilter.numHashRounds
      recoveredFilter.sizeBits mustBe bloomFilter.sizeBits
    }

  }

}
