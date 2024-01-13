package scodec.bits

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BinaryReaderWriterSpec extends AnyWordSpec with Matchers {

  val exp = TestData.TestInstance()

  private def writeTestInstance(instance: TestData.TestInstance): Array[Byte] = {
    BinaryWriter.newWriter
      .write(exp.byteArray)
      .write(exp.boolean)
      .write(exp.byte)
      .write(exp.short)
      .write(exp.int)
      .write(exp.long)
      .write(exp.float)
      .write(exp.double)
      .write(exp.uuid)
      .write(exp.string)
      .write(exp.instant)
      .writeOptionOf.short(exp.someShort)
      .writeOptionOf.short(exp.noneShort)
      .writeOptionOf.long(exp.someLong)
      .writeOptionOf.long(exp.noneLong)
      .writeOptionOf.string(exp.someString)
      .writeOptionOf.string(exp.noneString)
      .toByteArray
  }

  "BinaryWriter" should {

    "be able to write all TestInstance fields" in {
      val result = writeTestInstance(exp)
      result.length must be > 0
    }

    "be able to write all TestInstance fields 100,000 times" in {
      var count = 0L
      for(_ <- 1 to 100000) {
        val result = writeTestInstance(exp)
        count += result.length
      }

      count must be > 0L
    }

  }

  "BinaryReader" should {

    "be able to read all TestInstance fields" in {
      val binary = writeTestInstance(exp)

      val reader = BinaryReader(binary)
      val actual = TestData.TestInstance(
        reader.readBytes,
        reader.readBoolean,
        reader.readByte,
        reader.readShort,
        reader.readInt,
        reader.readLong,
        reader.readFloat,
        reader.readDouble,
        reader.readUUID,
        reader.readString,
        reader.readInstant,
        reader.readShortOption,
        reader.readShortOption,
        reader.readLongOption,
        reader.readLongOption,
        reader.readStringOption,
        reader.readStringOption
      )

      actual.equals(exp) mustBe true
    }

    "be able to read all TestInstance fields 100,000 times" in {
      val binary = writeTestInstance(exp)
      var count = 0L

      for(i <- 1 to 100000) {
        val reader = BinaryReader(binary)
        val actual = TestData.TestInstance(
          reader.readBytes,
          reader.readBoolean,
          reader.readByte,
          reader.readShort,
          reader.readInt,
          reader.readLong,
          reader.readFloat,
          reader.readDouble,
          reader.readUUID,
          reader.readString,
          reader.readInstant,
          reader.readShortOption,
          reader.readShortOption,
          reader.readLongOption,
          reader.readLongOption,
          reader.readStringOption,
          reader.readStringOption
        )

        count += actual.long + i
      }

      count must be > 0L
    }

  }

}
