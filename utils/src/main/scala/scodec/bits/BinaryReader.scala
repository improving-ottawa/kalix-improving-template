package scodec.bits

import java.nio.ByteBuffer
import java.nio.charset.{Charset, StandardCharsets}
import java.time._
import java.util.UUID

final class BinaryReader private (private val reader: ByteBuffer) extends AnyVal {
  def position: Int = reader.position()
  def remainingBytes: Int = reader.remaining()
  def endOfData: Boolean = reader.remaining() == 0

  def readBytes: ByteVector = ByteVector.view(readByteArray)

  def readByteArray: Array[Byte] = {
    val size = readInt
    val outArray = new Array[Byte](size)
    reader.get(outArray)
    outArray
  }

  def readBoolean: Boolean = reader.get != 0

  def readByte: Byte = reader.get

  def readShort: Short = reader.getShort

  def readInt: Int = reader.getInt

  def readLong: Long = reader.getLong

  def readFloat: Float = reader.getFloat

  def readDouble: Double = reader.getDouble

  def readUUID: UUID = {
    val highBits = readLong
    val lowBits = readLong
    new UUID(highBits, lowBits)
  }

  def readInstant: Instant = {
    val seconds = readLong
    val nanos = readInt
    Instant.ofEpochSecond(seconds, nanos)
  }

  def readString: String = {
    val strBytes = readByteArray
    new String(strBytes, StandardCharsets.UTF_8)
  }

  def readStringCharset(charset: Charset = StandardCharsets.UTF_8): String = {
    val strBytes = readByteArray
    new String(strBytes, charset)
  }

  def readByteArrayOption: Option[Array[Byte]] = readOptionInternal(readByteArray)

  def readBytesOption: Option[ByteVector] = {
    val isDefined = readBoolean
    if (isDefined) Some(ByteVector.view(readByteArray)) else None
  }

  def readByteOption: Option[Byte] = readOptionInternal(readByte)

  def readShortOption: Option[Short] = readOptionInternal(readShort)

  def readIntOption: Option[Int] = readOptionInternal(readInt)

  def readLongOption: Option[Long] = readOptionInternal(readLong)

  def readFloatOption: Option[Float] = readOptionInternal(readFloat)

  def readDoubleOption: Option[Double] = readOptionInternal(readDouble)

  def readUUIDOption: Option[UUID] = readOptionInternal(readUUID)

  def readInstantOption: Option[Instant] = readOptionInternal(readInstant)

  def readStringOption: Option[String] = readOptionInternal(readString)

  def readStringCharsetOption(charset: Charset = StandardCharsets.UTF_8): Option[String] =
    readOptionInternal(readStringCharset(charset))

  // option reader helper

  @inline private def readOptionInternal[A](readFn: => A): Option[A] =
    if (readBoolean) Some(readFn) else None

}

object BinaryReader {

  def apply(bytes: ByteVector): BinaryReader = new BinaryReader(bytes.toByteBuffer)

  def apply(byteArray: Array[Byte]): BinaryReader = new BinaryReader(ByteBuffer.wrap(byteArray))

}
