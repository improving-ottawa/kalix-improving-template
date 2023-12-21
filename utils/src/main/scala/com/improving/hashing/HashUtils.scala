package com.improving.hashing

import java.nio.ByteBuffer
import scala.annotation.switch

private[hashing] object HashUtils {

  @inline final def readInt64(data: Array[Byte], index: Int): Long = {
    (data(index).toLong & 0xff) |
      ((data(index + 1).toLong & 0xff) << 8) |
      ((data(index + 2).toLong & 0xff) << 16) |
      ((data(index + 3).toLong & 0xff) << 24) |
      ((data(index + 4).toLong & 0xff) << 32) |
      ((data(index + 5).toLong & 0xff) << 40) |
      ((data(index + 6).toLong & 0xff) << 48) |
      ((data(index + 7).toLong & 0xff) << 56)
  }

  @inline final def readInt64Partial(data: Array[Byte], index: Int): Long = {
    var remaining = data.length - index
    remaining = if (remaining > 8) 8 else remaining

    (remaining: @switch) match {
      case 8 => readInt64(data, index)

      case 7 =>
        ((data(index + 6).toLong & 0xff) << 48) |
        ((data(index + 5).toLong & 0xff) << 40) |
        ((data(index + 4).toLong & 0xff) << 32) |
        ((data(index + 3).toLong & 0xff) << 24) |
        ((data(index + 2).toLong & 0xff) << 16) |
        ((data(index + 1).toLong & 0xff) << 8) |
        (data(index).toLong & 0xff)

      case 6 =>
        ((data(index + 5).toLong & 0xff) << 40) |
        ((data(index + 4).toLong & 0xff) << 32) |
        ((data(index + 3).toLong & 0xff) << 24) |
        ((data(index + 2).toLong & 0xff) << 16) |
        ((data(index + 1).toLong & 0xff) << 8) |
        (data(index).toLong & 0xff)

      case 5 =>
        ((data(index + 4).toLong & 0xff) << 32) |
        ((data(index + 3).toLong & 0xff) << 24) |
        ((data(index + 2).toLong & 0xff) << 16) |
        ((data(index + 1).toLong & 0xff) << 8) |
        (data(index).toLong & 0xff)

      case 4 =>
        ((data(index + 3).toLong & 0xff) << 24) |
        ((data(index + 2).toLong & 0xff) << 16) |
        ((data(index + 1).toLong & 0xff) << 8) |
        (data(index).toLong & 0xff)

      case 3 =>
        ((data(index + 2).toLong & 0xff) << 16) |
        ((data(index + 1).toLong & 0xff) << 8) |
        (data(index).toLong & 0xff)

      case 2 =>
        ((data(index + 1).toLong & 0xff) << 8) |
        (data(index).toLong & 0xff)

      case 1 => data(index).toLong & 0xff

      case 0 => 0L
    }
  }

  final def packInt32s(a: Int, b: Int): Array[Byte] = {
    val buf = ByteBuffer.allocate(8)
    buf.putInt(a)
    buf.putInt(b)
    buf.array()
  }

  final def packInt64s(a: Long, b: Long): Array[Byte] = {
    val buf = ByteBuffer.allocate(16)
    buf.putLong(a)
    buf.putLong(b)
    buf.array()
  }

}
