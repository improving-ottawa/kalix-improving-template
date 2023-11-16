package com.example.utils

import java.security.SecureRandom
import java.util.Base64

object LoginTokenGenerator {
  final val bitLength = 256

  def generate(): String = {
    val gen    = new SecureRandom()
    val target = new Array[Byte](bitLength / 8)
    gen.nextBytes(target)
    // Using the `UrlEncoder` here so the token can be used in the login link
    Base64.getUrlEncoder.encodeToString(target)
  }

}
