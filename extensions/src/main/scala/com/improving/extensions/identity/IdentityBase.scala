package com.improving.extensions.identity

import java.util.UUID

trait IdentityBase {
  /** The [[UUID unique identifier]] for this identity. */
  def id: UUID

  /** The username of this identity. */
  def name: String

  /** Convert this [[IdentityBase identity]] into a byte array. */
  def toByteArray: Array[Byte]
}

trait IdentityBaseCodec[A <: IdentityBase] {

  def toBinary(instance: A): Array[Byte] = instance.toByteArray

  def fromBinary(bytes: Array[Byte]): Either[Throwable, A]

}
