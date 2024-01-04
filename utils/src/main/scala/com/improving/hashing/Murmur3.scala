package com.improving.hashing

/**
  * Fast / performant 64-bit hashing implementation of MurmurHash3
  *
  * Derived from:
  * https://github.com/google/guava/blob/fa95e381e665d8ee9639543b99ed38020c8de5ef/guava/src/com/google/common/hash/Murmur3_128HashFunction.java
  */
object Murmur3 extends HashAlgorithm {
  import java.lang.Long.rotateLeft

  // Constants
  final val defaultSeed = 0L
  final private val C1  = 0x87c37b91114253d5L
  final private val C2  = 0x4cf5ad432745937fL

  /* Implementations */

  final def hashLong(value: Long, seed: Long = defaultSeed): Long = {
    var h1 = seed
    var h2 = seed

    h1 ^= mixK1(value)
    h2 ^= mixK2(0L)

    // result
    avalanche(8L, h1, h2)
  }

  final def hashLongs(data: Seq[Long], seed: Long = defaultSeed): Long = {
    val length = data.length
    var h1     = seed
    var h2     = seed
    var index  = 0

    if (length >= 2) {
      val limit = length - 2
      while (index <= limit) {
        val k1 = data(index)
        val k2 = data(index + 1)
        index += 2

        h1 ^= mixK1(k1)
        h1 = rotateLeft(h1, 27)
        h1 += h2
        h1 = h1 * 5L + 0x52dce729L

        h2 ^= mixK2(k2)

        h2 = rotateLeft(h2, 31)
        h2 += h1
        h2 = h2 * 5L + 0x38495ab5L
      }
    }

    // tail part
    if (index < length) {
      val k1 = data(index)
      index += 1

      h1 ^= mixK1(k1)
      h2 ^= mixK2(0L)
    }

    // result
    avalanche(length * 8L, h1, h2)
  }

  final def hashByteArray(data: Array[Byte], seed: Long = defaultSeed): Long = {
    val length = data.length
    var h1     = seed
    var h2     = seed
    var index  = 0

    if (length >= 16) {
      val limit = length - 16
      while (index <= limit) {
        val k1 = HashUtils.readInt64(data, index)
        val k2 = HashUtils.readInt64(data, index + 8)
        index += 16

        h1 ^= mixK1(k1)
        h1 = rotateLeft(h1, 27)
        h1 += h2
        h1 = h1 * 5L + 0x52dce729L

        h2 ^= mixK2(k2)

        h2 = rotateLeft(h2, 31)
        h2 += h1
        h2 = h2 * 5L + 0x38495ab5L
      }
    }

    // tail part
    while (index < length) {
      val remaining = length - index
      val k1        = HashUtils.readInt64Partial(data, index)
      val k2        = if (remaining > 8) HashUtils.readInt64Partial(data, index + 8) else 0L
      index += 16

      h1 ^= mixK1(k1)
      h2 ^= mixK2(k2)
    }

    // result
    avalanche(length, h1, h2)
  }

  /* Private/Internal functions */

  @inline final private def avalanche(length: Long, in1: Long, in2: Long): Long = {
    var h1 = in1 ^ length
    var h2 = in2 ^ length

    h1 += h2
    h2 += h1

    h1 = fmix64(h1)
    h2 = fmix64(h2)

    h1 + h2
  }

  @inline final private def fmix64(in: Long): Long = {
    var k = in ^ in >>> 33
    k *= 0xff51afd7ed558ccdL
    k ^= k >>> 33
    k *= 0xc4ceb9fe1a85ec53L
    k ^ k >>> 33
  }

  @inline final private def mixK1(k1: Long): Long = {
    var res = k1 * C1
    res = rotateLeft(res, 31)
    res * C2
  }

  @inline final private def mixK2(k2: Long): Long = {
    var res = k2 * C2
    res = rotateLeft(res, 33)
    res * C1
  }

}
