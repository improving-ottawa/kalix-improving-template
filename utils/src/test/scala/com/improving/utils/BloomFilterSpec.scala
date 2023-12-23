package com.improving.utils

import com.improving.hashing.XXHash

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.security.SecureRandom
import java.util.UUID

class BloomFilterSpec extends AnyWordSpec with Matchers {

  "BloomFilter" should {

    "create a (more-or-less) even distribution after being filled" in {
      val secureRng = new SecureRandom()
      val bloom     = BloomFilter[Long](2000000, 0.01, (lng, seed) => XXHash.hashLong(lng, seed))

      // Fill the bloom filter with 1,000,000 random longs
      val filledBloom = (1 to 1000000).foldLeft(bloom)((blm, _) => blm.add(secureRng.nextLong()))

      // Check the next 1,000,000 random UUIDs
      val falsePositives =
        (1 to 1000000).foldLeft(0L)((acc, _) => if (filledBloom.contains(secureRng.nextLong())) acc + 1 else acc)

      val actualErrorRate = falsePositives / 1000000.0

      actualErrorRate must be < 0.1
      actualErrorRate must be > 0.0
    }

  }

}
