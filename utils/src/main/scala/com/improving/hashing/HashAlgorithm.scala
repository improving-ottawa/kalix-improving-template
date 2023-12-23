package com.improving.hashing

abstract class HashAlgorithm {

  /** The default seed for this hashing algorithm */
  def defaultSeed: Long

  /* (abstract) Implementations */

  def hashLong(value: Long, seed: Long = defaultSeed): Long

  def hashLongs(data: Seq[Long], seed: Long = defaultSeed): Long

  def hashByteArray(data: Array[Byte], seed: Long = defaultSeed): Long

  /* (concrete) Implementations */

  @inline final def hashInt(value: Int, seed: Long = defaultSeed): Long = hashLong(value, seed)

  final def hashInts(data: Seq[Int], seed: Long = defaultSeed): Long =
    hashLongs(data.map(_.toLong), seed)

  @inline final def objectHash(ref: Any, seed: Long = defaultSeed): Int = hashLong(ref.hashCode(), seed).toInt

}
