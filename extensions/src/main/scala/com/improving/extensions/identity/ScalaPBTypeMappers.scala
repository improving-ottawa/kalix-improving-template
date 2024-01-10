package com.improving.extensions.identity

import scalapb.TypeMapper

import java.time.Instant
import java.util.UUID

trait CredentialTypeProtoCompanion { self: CredentialTypeProto.type =>

  implicit final val typeMapperForCredentialType: TypeMapper[CredentialTypeProto, CredentialType] =
    TypeMapper(CredentialType.fromProto)(CredentialType.toProto)

}

object ScalaPBTypeMappers {

  implicit final val typeMapperStringUUID: TypeMapper[String, UUID] =
    TypeMapper[String, UUID](str => UUID.fromString(str))(uuid => uuid.toString)

  implicit final val typeMapperLongInstant: TypeMapper[Long, Instant] =
    TypeMapper[Long, Instant](epochMillis => Instant.ofEpochMilli(epochMillis))(instant => instant.toEpochMilli)

}
