package com.improving.utils

import java.util.Base64

/** Strongly typed `Base64` textual representation of data. */
abstract class Base64String private[utils] extends CharSequence {
  /** Indicates whether or not this [[Base64String]] textual representation is safe for URL encoding. */
  def urlSafe: Boolean

  /** The textual (string) representation of this [[Base64String]]. */
  def toString: String

  /** The raw (underlying) bytes of this [[Base64String]]. */
  def rawBytes: Array[Byte]

  final def length(): Int = toString.length

  final def charAt(index: Int): Char = toString.charAt(index)

  final def subSequence(start: Int, end: Int): CharSequence = toString.subSequence(start, end)
}

object Base64String {

  def apply(data: Array[Byte], urlSafe: Boolean = true, withoutPadding: Boolean = true): Base64String =
    Impl(getEncoder(urlSafe, withoutPadding).encodeToString(data), urlSafe)

  def fromBase64String(base64: String, urlSafe: Boolean = true): Either[Throwable, Base64String] =
    try Right(unsafeFromBase64String(base64, urlSafe))
    catch { case scala.util.control.NonFatal(error) => Left(error) }

  def unsafeFromBase64String(base64: String, urlSafe: Boolean = true): Base64String = {
    val decoder = if (urlSafe) Base64.getUrlDecoder else Base64.getDecoder
    val rawBytes = decoder.decode(base64)

    assert(rawBytes.length >= 0)
    Impl(base64, urlSafe)
  }

  // Base64 Encoder selected from formatting arguments
  private[utils] final def getEncoder(urlSafe: Boolean, withoutPadding: Boolean): Base64.Encoder =
    (urlSafe, withoutPadding) match {
      case (true, true) => Base64.getUrlEncoder.withoutPadding()
      case (true, false) => Base64.getUrlEncoder
      case (false, true) => Base64.getEncoder.withoutPadding()
      case (false, false) => Base64.getEncoder
    }

  private final case class Impl(override val toString: String, urlSafe: Boolean) extends Base64String {

    override lazy val rawBytes: Array[Byte] = {
      val decoder = if (urlSafe) Base64.getUrlDecoder else Base64.getDecoder
      decoder.decode(toString)
    }

    override def canEqual(that: Any): Boolean = that.isInstanceOf[Base64String]

    override def equals(obj: Any): Boolean =
      obj match {
        case impl: Impl         => impl.toString == toString
        case that: Base64String => java.util.Arrays.equals(that.rawBytes, rawBytes)
        case _                  => false
      }

    override def hashCode(): Int = 1777 * scala.util.hashing.MurmurHash3.stringHash(toString)
  }

}
