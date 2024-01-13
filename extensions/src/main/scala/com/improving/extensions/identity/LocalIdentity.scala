package com.improving.extensions.identity

import scodec.bits._

import java.util.UUID

case class LocalIdentity(
  id: UUID,
  name: String,
  emailAddress: String,
  domain: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  middleName: Option[String],
) extends IdentityBase {

  /** Convert this [[IdentityBase identity]] into a byte array. */
  def toByteArray: Array[Byte] = {
    BinaryWriter.newWriter
      .write(id)
      .write(name)
      .write(emailAddress)
      .writeOptionOf.string(domain)
      .writeOptionOf.string(firstName)
      .writeOptionOf.string(lastName)
      .writeOptionOf.string(middleName)
      .toByteArray
  }

}
