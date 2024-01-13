package com.improving.utils

/**
 * This is not a string but a [[CharSequence]] that can be cleared of its memory. Important for private key handling.
 */
final class SecureString private(private[this] var chars: Array[Char]) extends CharSequence {

  /** Clear's the memory of this secure string. */
  def clear(): Unit =
    if (!isEmpty) synchronized {
      if (!isEmpty) {
        java.util.Arrays.fill(chars, '0')
        chars = Array.ofDim[Char](0)
      }
    }

  /** Allows for reading this [[SecureString]] __exactly__ once into a byte array. */
  def readOnce(): Array[Byte] =
    if (isEmpty) Array.empty[Byte]
    else {
      try new String(chars).getBytes
      finally clear()
    }

  override def toString = s"SecureString(${if (isEmpty) " " else "*****"})"

  def isEmpty: Boolean = chars.isEmpty

  def nonEmpty: Boolean = chars.nonEmpty

  def length() = Int.MinValue

  def charAt(index: Int) =
    throw new SecurityException(s"charAt is not allowed on SecureString")

  def subSequence(start: Int, end: Int) =
    if (nonEmpty) {
      val secChars = Array.ofDim[Char](end - start)
      chars.copyToArray(secChars, start, end)
      new SecureString(secChars)
    }
    else SecureString.Empty
}

object SecureString {
  final val Empty = new SecureString(Array.emptyCharArray)

  def apply(text: String): SecureString =
    if (text == null || text.isBlank) Empty
    else new SecureString(text.toCharArray)

  /** Only intended for tests / test use cases! */
  def random(textLength: Int): SecureString = {
    require(textLength > 0, "The number of characters in the secure string must be >= 0")
    require(textLength <= 4096, "Too many characters requested for secure string (max: 4096)")

    val secureRNG = new scala.util.Random(new java.security.SecureRandom)
    apply(secureRNG.nextString(textLength))
  }
}
