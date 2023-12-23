package com.improving.hashing

/**
  * Fast / performant 64-bit hashing implementation of XXHash
  *
  * Derived from: https://cyan4973.github.io/xxHash
  */
object XXHash extends HashAlgorithm {
  import java.lang.Long.rotateLeft

  // Constants
  final val defaultSeed       = 0L
  final private val PRIME64_1 = 0x9e3779b185ebca87L
  final private val PRIME64_2 = 0xc2b2ae3d27d4eb4fL
  final private val PRIME64_3 = 0x165667b19e3779f9L
  final private val PRIME64_4 = 0x85ebca77c2b2ae63L
  final private val PRIME64_5 = 0x27d4eb2f165667c5L

  /* Implementations */

  final def hashLong(value: Long, seed: Long = defaultSeed): Long = {
    var hash = PRIME64_5 + seed + 8
    hash = updateTail(hash, value)
    finalShuffle(hash)
  }

  final def hashLongs(data: Seq[Long], seed: Long = defaultSeed): Long = {
    val length = data.length
    var hash   = 0L
    var index  = 0

    if (length >= 4) {
      var v1    = seed + PRIME64_1 + PRIME64_2
      var v2    = seed + PRIME64_2
      var v3    = seed + 0L
      var v4    = seed - PRIME64_1
      val limit = length - 4
      do {
        val k1 = data(index)
        v1 = mix(v1, k1)
        index += 1
        val k2 = data(index)
        v2 = mix(v2, k2)
        index += 1
        val k3 = data(index)
        v3 = mix(v3, k3)
        index += 1
        val k4 = data(index)
        v4 = mix(v4, k4)
        index += 1
      } while (index <= limit)

      hash = rotateLeft(v1, 1) + rotateLeft(v2, 7) + rotateLeft(v3, 12) + rotateLeft(v4, 18)
      hash = update(hash, v1)
      hash = update(hash, v2)
      hash = update(hash, v3)
      hash = update(hash, v4)
    } else hash = seed + PRIME64_5

    hash += length * 8

    // tail
    while (index < length) {
      val k1 = data(index)
      hash = updateTail(hash, k1)
      index += 1
    }

    finalShuffle(hash)
  }

  final def hashByteArray(data: Array[Byte], seed: Long = defaultSeed): Long = {
    val length = data.length
    var hash   = 0L
    var index  = 0

    if (length >= 32) {
      var v1    = seed + PRIME64_1 + PRIME64_2
      var v2    = seed + PRIME64_2
      var v3    = seed + 0L
      var v4    = seed - PRIME64_1
      val limit = length - 32
      do {
        val k1 = HashUtils.readInt64(data, index)
        v1 = mix(v1, k1)
        index += 8
        val k2 = HashUtils.readInt64(data, index)
        v2 = mix(v2, k2)
        index += 8
        val k3 = HashUtils.readInt64(data, index)
        v3 = mix(v3, k3)
        index += 8
        val k4 = HashUtils.readInt64(data, index)
        v4 = mix(v4, k4)
        index += 8
      } while (index <= limit)

      hash = rotateLeft(v1, 1) + rotateLeft(v2, 7) + rotateLeft(v3, 12) + rotateLeft(v4, 18)
      hash = update(hash, v1)
      hash = update(hash, v2)
      hash = update(hash, v3)
      hash = update(hash, v4)
    } else hash = seed + PRIME64_5

    hash += length

    // tail
    while (index <= length - 8) {
      val k = HashUtils.readInt64Partial(data, index)
      hash = updateTail(hash, k)
      index += 8
    }

    if (index <= length - 4) {
      val tailStart = index
      var remaining = length - index
      remaining = if (remaining > 4) 4 else remaining
      var k         = 0
      if (remaining == 4) k |= (data(tailStart + 3) & 0xff) << 24
      if (remaining > 2) k |= (data(tailStart + 2) & 0xff) << 16
      if (remaining > 1) k |= (data(tailStart + 1) & 0xff) << 8
      if (remaining > 0) k |= (data(tailStart) & 0xff)

      hash = updateTail(hash, k)
      index += 4
    }

    while (index < length) {
      hash = updateTail(hash, data(index))
      index += 1
    }

    finalShuffle(hash)
  }

  /* Private/Internal functions */

  @inline final private def mix(current: Long, value: Long) =
    rotateLeft(current + value * PRIME64_2, 31) * PRIME64_1

  @inline final private def update(hash: Long, value: Long) = {
    val temp = hash ^ mix(0, value)
    temp * PRIME64_1 + PRIME64_4
  }

  @inline final private def updateTail(hash: Long, value: Long) = {
    val temp = hash ^ mix(0L, value)
    rotateLeft(temp, 27) * PRIME64_1 + PRIME64_4
  }

  @inline final private def updateTail(hash: Long, value: Int) = {
    val unsigned = value & 0xffffffffL
    val temp     = hash ^ (unsigned * PRIME64_1)
    rotateLeft(temp, 23) * PRIME64_2 + PRIME64_3
  }

  @inline final private def updateTail(hash: Long, value: Byte) = {
    val unsigned = value & 0xff
    val temp     = hash ^ (unsigned * PRIME64_5)
    rotateLeft(temp, 11) * PRIME64_1
  }

  @inline final private def finalShuffle(hash: Long) = {
    var _hash = hash
    _hash ^= _hash >>> 33
    _hash *= PRIME64_2
    _hash ^= _hash >>> 29
    _hash *= PRIME64_3
    _hash ^ (_hash >>> 32)
  }

}
