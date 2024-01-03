package com.improving.utils

import com.improving.hashing._

import scodec.bits.BitVector
import scala.annotation.tailrec

/**
  * A space efficient Bloom Filter.
  *
  * @see
  *   https://en.wikipedia.org/wiki/Bloom_filter
  */
final class BloomFilter[@specialized A] private[utils] (
  hashBits: BitVector,
  hashFunction: BloomFilter.HashFunction[A],
  val numHashRounds: Int
) {
  /* Public API */

  val sizeBits: Long = hashBits.size

  def truthRatio: Double = 1.0 - (trueBits / sizeBits.toDouble)

  def clear(): BloomFilter[A] = {
    val emptyBitVector = BitVector.low(hashBits.size)
    new BloomFilter[A](emptyBitVector, hashFunction, numHashRounds)
  }

  def contains(item: A): Boolean = {
    @tailrec def loop(cnt: Int): Boolean =
      if (cnt == 0) true
      else if (!hashBits.get(computeHash(item, cnt))) false
      else loop(cnt - 1)

    loop(numHashRounds)
  }

  def add(item: A): BloomFilter[A] = {
    @tailrec def loop(cnt: Int, bitVector: BitVector): BitVector =
      if (cnt == 0) bitVector
      else loop(cnt - 1, bitVector.set(computeHash(item, cnt)))

    new BloomFilter[A](loop(numHashRounds, hashBits), hashFunction, numHashRounds)
  }

  def toByteArray: Array[Byte] = {
    val baos   = new java.io.ByteArrayOutputStream()
    val writer = new java.io.DataOutputStream(baos)
    try {
      writer.writeLong(sizeBits)
      writer.writeInt(numHashRounds)
      writer.write(hashBits.toByteArray)
      writer.flush()
      baos.toByteArray
    } finally {
      writer.close()
      baos.close()
    }
  }

  /* Private/Internal Functions */

  private def trueBits: Int = {
    var acc   = 0
    var index = 0
    val bv    = hashBits.toByteVector
    while (index < bv.length) {
      val b = bv(index)
      acc += (b >> 0 & 1) +
        (b >> 1 & 1) +
        (b >> 2 & 1) +
        (b >> 3 & 1) +
        (b >> 4 & 1) +
        (b >> 5 & 1) +
        (b >> 6 & 1) +
        (b >> 7 & 1)

      index += 1
    }
    acc
  }

  @inline private def computeHash(obj: A, round: Int): Long =
    math.abs(hashFunction(obj, round) % sizeBits)

}

object BloomFilter {
  final type HashFunction[A] = (A, Int) => Long

  // The default hashing functions
  object DefaultHashFunctions {
    final def murmur3Hash[A]: HashFunction[A] = (obj, seed) => Murmur3.hashLong(obj.hashCode(), seed)
    final def xxHash[A]: HashFunction[A]      = (obj, seed) => XXHash.hashLong(obj.hashCode(), seed)
  }

  def apply[A](
    capacity: Int,
    errorRate: Double,
    hashFunction: HashFunction[A] = DefaultHashFunctions.xxHash[A]
  ): BloomFilter[A] = {
    assert(capacity > 0, "`capacity` must be > 0")
    assert(errorRate > 0 && errorRate < 1.0, "`errorRate` must be between [0, 1]")
    apply(capacity, errorRate, hashFunction, computeBestK(capacity, errorRate))
  }

  def apply[A](capacity: Int, errorRate: Double, hashFunction: HashFunction[A], numHashRounds: Int): BloomFilter[A] =
    new BloomFilter[A](
      BitVector.low(computeBestM(capacity, errorRate)),
      hashFunction,
      numHashRounds
    )

  def fromByteArray[A](
    array: Array[Byte],
    hashFunction: HashFunction[A] = DefaultHashFunctions.xxHash[A]
  ): BloomFilter[A] = {
    val bais   = new java.io.ByteArrayInputStream(array)
    val reader = new java.io.DataInputStream(bais)
    try {
      val bitSize    = reader.readLong()
      val hashRounds = reader.readInt()
      val byteArray  = Array.ofDim[Byte](reader.available)
      reader.read(byteArray)
      val padded     = BitVector(byteArray)
      val corrected  = padded.dropRight(padded.size.toInt - bitSize)
      new BloomFilter[A](corrected, hashFunction, hashRounds)
    } finally {
      reader.close()
      bais.close()
    }
  }

  /* Private/Internal functions */

  final private val log2     = math.log(2.0)
  final private val two_log2 = math.pow(2, log2)

  final private def computeBestM(capacity: Int, errorRate: Double): Int = {
    val factor = -(capacity.toDouble * math.log(errorRate)) / two_log2
    assert(factor > 0)
    factor.toInt
  }

  final private def computeBestK(n: Int, errorRate: Double): Int = {
    val m = computeBestM(n, errorRate).toDouble
    val k = (m / n) * log2
    math.round(k).toInt
  }

}
