package com.improving.utils

import java.security.SecureRandom

object SecureRandomString {

  /** Default entropy length for a new [[SecureRandomString]]. */
  final val defaultEntropyLength = 64

  final def apply(
    byteLength: Int = defaultEntropyLength,
    usePadding: Boolean = false,
    urlSafe: Boolean = true,
  ): Base64String = {
    require(byteLength > 0, "The number of bytes in the secure string must be >= 0")
    require(byteLength <= 4096, "Too many bytes requested for secure string (max: 4096)")

    val secureRng = new SecureRandom()

    val byteArray = Array.ofDim[Byte](byteLength)
    secureRng.nextBytes(byteArray)

    Base64String(byteArray, urlSafe, usePadding)
  }

}
