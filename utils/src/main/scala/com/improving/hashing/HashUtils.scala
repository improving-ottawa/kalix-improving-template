package com.improving.hashing

import scala.annotation.switch

import java.lang.Long.{reverseBytes => swap64}
import java.lang.Integer.{reverseBytes => swap32}
import java.lang.reflect.Field
import java.nio.ByteOrder
import sun.misc.Unsafe

private[hashing] object HashUtils {

  private[this] final val theUnsafe = {
    val field: Field = classOf[Unsafe].getDeclaredField("theUnsafe")
    field.setAccessible(true)
    field.get(null).asInstanceOf[Unsafe]
  }

  private[this] final val isLittleEndian = ByteOrder.nativeOrder == ByteOrder.LITTLE_ENDIAN

  final def readInt64(data: Array[Byte], index: Int): Long = {
    if (isLittleEndian) {
      theUnsafe.getLong(data, index)
    } else {
      swap64(theUnsafe.getLong(data, index))
    }
  }

  final def readUnsignedInt32(data: Array[Byte], index: Int): Int = {
    if (isLittleEndian) {
      theUnsafe.getInt(data, index)
    } else {
      swap32(theUnsafe.getInt(data, index))
    }
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

}
