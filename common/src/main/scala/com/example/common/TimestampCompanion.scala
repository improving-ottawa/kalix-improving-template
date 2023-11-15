package com.example.common

import scalapb._

import java.time.Instant

trait TimestampCompanion extends GeneratedMessageCompanion[Timestamp] {

  /** The [[TypeMapper]] for [[Timestamp]] <==> [[java.time.Instant]] */
  implicit final val timestampInstantMapper: TypeMapper[Timestamp, Instant] =
    new TypeMapper[Timestamp, Instant] {
      final def toCustom(ts: Timestamp): Instant = Instant.ofEpochSecond(ts.seconds, ts.nanos)
      final def toBase(ins: Instant): Timestamp = Timestamp(ins.getEpochSecond, ins.getNano)
    }

}
