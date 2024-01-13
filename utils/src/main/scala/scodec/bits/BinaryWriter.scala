package scodec.bits

import akka.util.{ByteString, ByteStringBuilder}

import java.nio.charset.{Charset, StandardCharsets}
import java.time._
import java.util.UUID

final class BinaryWriter private (private val writer: ByteStringBuilder) extends AnyVal {
  import BinaryWriter._

  def toByteVector: ByteVector = ByteVector(toByteArray)

  def toByteArray: Array[Byte] = writer.result().toArrayUnsafe()

  def toByteString: ByteString = writer.result()

  def length: Int = writer.length

  def write(b: Byte): BinaryWriter = { writer.putByte(b); this }

  def write(b: Boolean): BinaryWriter = {
    writer.putByte(if (b) trueByte else falseByte)
    this
  }

  def write(s: Short): BinaryWriter = { writer.putShort(s); this }

  def write(i: Int): BinaryWriter = { writer.putInt(i); this }

  def write(l: Long): BinaryWriter = { writer.putLong(l); this }

  def write(f: Float): BinaryWriter = { writer.putFloat(f); this }

  def write(d: Double): BinaryWriter = { writer.putDouble(d); this }

  def write(array: Array[Byte]): BinaryWriter =
    if (array.isEmpty) { writer.putInt(0); this }
    else {
      writer.putInt(array.length)
      writer.putBytes(array)
      this
    }

  def write(byteVector: ByteVector): BinaryWriter =
    if (byteVector.isEmpty) { writer.putInt(0); this }
    else {
      writer.putInt(byteVector.length.toInt)
      writer.putBytes(byteVector.toArrayUnsafe)
      this
    }

  def write(s: String, charset: Charset = StandardCharsets.UTF_8): BinaryWriter = {
    val stringData = s.getBytes(charset)
    val strLength  = stringData.length

    writer.putInt(strLength)
    writer.putBytes(stringData)
    this
  }

  def write(uuid: UUID): BinaryWriter = {
    writer.putLong(uuid.getMostSignificantBits)
    writer.putLong(uuid.getLeastSignificantBits)
    this
  }

  def write(instant: Instant): BinaryWriter = {
    writer.putLong(instant.getEpochSecond)
    writer.putInt(instant.getNano)
    this
  }

  def writeOptionOf: BinaryWriter.BinaryWriterOption = new BinaryWriter.BinaryWriterOption(writer)
}

object BinaryWriter {
  final private val trueByte: Byte  = 1
  final private val falseByte: Byte = 0

  implicit final private val byteOrder: java.nio.ByteOrder = java.nio.ByteOrder.BIG_ENDIAN

  final def newWriter: BinaryWriter = new BinaryWriter(new ByteStringBuilder)

  @inline final private def fromBuffer(builder: ByteStringBuilder): BinaryWriter = new BinaryWriter(builder)

  final class BinaryWriterOption(private val buffer: ByteStringBuilder) extends AnyVal {
    @inline private def backToWriter: BinaryWriter = new BinaryWriter(buffer)

    def byte(b: Option[Byte]): BinaryWriter = b match {
      case Some(b) => backToWriter.write(true).write(b)
      case None    => backToWriter.write(false)
    }

    def short(s: Option[Short]): BinaryWriter = s match {
      case Some(b) => backToWriter.write(true).write(b)
      case None    => backToWriter.write(false)
    }

    def int(i: Option[Int]): BinaryWriter = i match {
      case Some(b) => backToWriter.write(true).write(b)
      case None    => backToWriter.write(false)
    }

    def long(l: Option[Long]): BinaryWriter = l match {
      case Some(b) => backToWriter.write(true).write(b)
      case None    => backToWriter.write(false)
    }

    def float(f: Option[Float]): BinaryWriter = f match {
      case Some(b) => backToWriter.write(true).write(b)
      case None    => backToWriter.write(false)
    }

    def double(d: Option[Double]): BinaryWriter = d match {
      case Some(b) => backToWriter.write(true).write(b)
      case None    => backToWriter.write(false)
    }

    def uuid(u: Option[UUID]): BinaryWriter = u match {
      case Some(b) => backToWriter.write(true).write(b)
      case None    => backToWriter.write(false)
    }

    def string(s: Option[String]): BinaryWriter = s match {
      case Some(b) => backToWriter.write(true).write(b)
      case None    => backToWriter.write(false)
    }

    def instant(i: Option[Instant]): BinaryWriter = i match {
      case Some(b) => backToWriter.write(true).write(b)
      case None    => backToWriter.write(false)
    }

  }

}
