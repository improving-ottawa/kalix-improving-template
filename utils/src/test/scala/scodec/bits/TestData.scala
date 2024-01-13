package scodec.bits

object TestData {

  val expectedByteArray = scala.util.Random.nextBytes(32)
  val expectedBoolean = true
  val expectedByte = 3.toByte
  val expectedShort = 1331.toShort
  val expectedInt = -32767
  val expectedLong = Int.MaxValue * 2L
  val expectedFloat = Math.PI.toFloat
  val expectedDouble = Math.PI
  val expectedUUID = java.util.UUID.randomUUID
  val expectedString = "Test String"
  val expectedInstant = java.time.Instant.now

  case class TestInstance(
    byteArray: ByteVector = ByteVector(expectedByteArray),
    boolean: Boolean = expectedBoolean,
    byte: Byte = expectedByte,
    short: Short = expectedShort,
    int: Int = expectedInt,
    long: Long = expectedLong,
    float: Float = expectedFloat,
    double: Double = expectedDouble,
    uuid: java.util.UUID = expectedUUID,
    string: String = expectedString,
    instant: java.time.Instant = expectedInstant,
    someShort: Option[Short] = Some(expectedShort),
    noneShort: Option[Short] = None,
    someLong: Option[Long] = Some(expectedLong),
    noneLong: Option[Long] = None,
    someString: Option[String] = Some(expectedString),
    noneString: Option[String] = None
  )

}
