package com.improving.utils

import java.util.Base64

/**
 * Strongly typed `Base64` textual representation of binary data.
 *
 * @param rawBytes The raw (underlying) bytes of this [[Base64String]].
 *
 * @param usePadding Indicates whether or not this [[Base64String]] will include padding in its Base64 representation.
 *
 * @param urlSafe Indicates whether or not this [[Base64String]] textual representation is safe for URL encoding.
 */
final class Base64String private(
  val rawBytes: Array[Byte],
  val usePadding: Boolean,
  val urlSafe: Boolean
) extends CharSequence with Product3[Array[Byte], Boolean, Boolean] {
  /** Returns a [[Base64String]] which is safe for URL encoding. */
  def makeSafe: Base64String = if (urlSafe) this else new Base64String(rawBytes.clone(), usePadding, true)

  /** Returns a [[Base64String]] which does not have padding in its textual (string) representation. */
  def withoutPadding: Base64String = if (!usePadding) this else new Base64String(rawBytes.clone(), false, urlSafe)

  /** The textual (string) representation of this [[Base64String]]. */
  override def toString: String = cachedBase64Text

  /** Returns true if this [[Base64String]] is empty. */
  def isEmpty: Boolean = rawBytes.isEmpty

  override def equals(obj: Any): Boolean = obj match {
    case b64: Base64String      => equals(b64)
    case str: String            => str == cachedBase64Text
    case charSeq: CharSequence  => CharSequence.compare(charSeq, this) == 0
    case _                      => false
  }

  def equals(other: Base64String): Boolean =
    java.util.Arrays.equals(other.rawBytes, rawBytes) &&
    other.usePadding == usePadding &&
    other.urlSafe == urlSafe

  override def hashCode(): Int = scala.util.hashing.MurmurHash3.productHash(this)

  /** The length of the [[rawBytes]] underlying this [[Base64String]]. */
  def rawLength: Int = rawBytes.length

  def length: Int = cachedBase64Text.length

  def charAt(index: Int): Char = cachedBase64Text.charAt(index)

  def subSequence(start: Int, end: Int): CharSequence = cachedBase64Text.subSequence(start, end)

  /* Product / Product3 Implementation */

  def _1: Array[Byte] = rawBytes

  def _2: Boolean = usePadding

  def _3: Boolean = urlSafe

  def canEqual(that: Any): Boolean = that match {
    case _: Base64String => true
    case _: String       => true
    case _: CharSequence => true
    case _               => false
  }

  override def productElementName(n: Int): String = n match {
    case 0 => "rawBytes"
    case 1 => "usePadding"
    case 2 => "urlSafe"
    case _ => throw new IndexOutOfBoundsException(s"$n is out of bounds (min 0, max 2)")
  }

  /* Internal Implementation */

  private lazy val cachedBase64Text: String =
    Base64String.getEncoder(urlSafe, usePadding).encodeToString(rawBytes)

}

object Base64String {

  def apply(data: Array[Byte], urlSafe: Boolean = true, usePadding: Boolean = false): Base64String =
    new Base64String(data, usePadding, urlSafe)

  def fromBase64String(base64: String): Either[Throwable, Base64String] =
    try Right(unsafeFromBase64String(base64))
    catch { case scala.util.control.NonFatal(error) => Left(error) }

  def unsafeFromBase64String(base64: String): Base64String = {
    val urlSafe = !(base64.contains('+') || base64.contains('/'))
    val decoder = if (urlSafe) Base64.getUrlDecoder else Base64.getDecoder

    val rawBytes = decoder.decode(base64)
    val hasPadding = base64.contains('=')

    assert(rawBytes.length >= 0)
    new Base64String(rawBytes, hasPadding, urlSafe)
  }

  // Base64 Encoder selected from formatting arguments
  private final def getEncoder(urlSafe: Boolean, usePadding: Boolean): Base64.Encoder =
    (urlSafe, usePadding) match {
      case (true, false)  => Base64.getUrlEncoder.withoutPadding()
      case (true, true)   => Base64.getUrlEncoder
      case (false, false) => Base64.getEncoder.withoutPadding()
      case (false, true)  => Base64.getEncoder
    }

}
