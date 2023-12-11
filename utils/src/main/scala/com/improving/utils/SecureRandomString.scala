package com.improving.utils

import java.security.SecureRandom

object SecureRandomString {

  /** Default entropy length for a new [[SecureRandomString]]. */
  final val defaultEntropyLength = 64

  final def apply(
    byteLength: Int = defaultEntropyLength,
    urlSafe: Boolean = true,
    withoutPadding: Boolean = true
  ): Base64String = {
    require(byteLength > 0, "The number of bytes in the secure string must be >= 0")
    require(byteLength <= 4096, "Too many bytes requested for secure string (max: 4096)")

    val byteArray = Array.ofDim[Byte](byteLength)
    val rng = rngSource.get()
    rng.nextBytes(byteArray)

    Base64String(byteArray, urlSafe, withoutPadding)
  }

  // Secure random source
  final private[this] val rngSource: ThreadLocal[SecureRandom] = ThreadLocal.withInitial(() => new SecureRandom())

}
