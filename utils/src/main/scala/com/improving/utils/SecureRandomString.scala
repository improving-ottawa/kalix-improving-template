package com.improving.utils

import java.security.SecureRandom
import java.util.Base64

final class SecureRandomString private (val rawBytes: Array[Byte], val urlSafe: Boolean, encoder: Base64.Encoder)
    extends Base64String {

  override lazy val toString: String = encoder.encodeToString(rawBytes)

  override def equals(obj: Any): Boolean =
    obj match {
      case that: Base64String => java.util.Arrays.equals(that.rawBytes, rawBytes)
      case _                  => false
    }

  override def hashCode(): Int = -1771 * scala.util.hashing.MurmurHash3.arrayHash(rawBytes)
}

object SecureRandomString {

  /** Default entropy length for a new [[SecureRandomString]]. */
  final val defaultEntropyLength = 64

  final def apply(
    byteLength: Int = defaultEntropyLength,
    urlSafe: Boolean = true,
    withoutPadding: Boolean = true
  ): SecureRandomString = {
    require(byteLength > 0, "The number of bytes in the secure string must be >= 0")
    require(byteLength <= 4096, "Too many bytes requested for secure string (max: 4096)")

    val byteArray = Array.ofDim[Byte](byteLength)
    val encoder   = Base64String.getEncoder(urlSafe, withoutPadding)
    val rng       = rngSource.get()
    rng.nextBytes(byteArray)

    new SecureRandomString(byteArray, urlSafe, encoder)
  }

  // Secure random source
  final private[this] val rngSource: ThreadLocal[SecureRandom] = ThreadLocal.withInitial(() => new SecureRandom())

}
