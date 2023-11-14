package com.example.common

import scalapb._

import java.util.concurrent.{TimeUnit => JTimeUnit}

trait TimeUnitCompanion extends GeneratedEnumCompanion[TimeUnit] {

  /** The [[TypeMapper]] for [[TimeUnit]] <==> [[java.util.concurrent.TimeUnit]] */
  implicit final val timeUnitMapper: TypeMapper[TimeUnit, JTimeUnit] =
    new TypeMapper[TimeUnit, JTimeUnit] {
      final def toCustom(base: TimeUnit): JTimeUnit = base match {
        case TimeUnit.TimeUnitNanoseconds  => JTimeUnit.NANOSECONDS
        case TimeUnit.TimeUnitMicroseconds => JTimeUnit.MICROSECONDS
        case TimeUnit.TimeUnitMilliseconds => JTimeUnit.MILLISECONDS
        case TimeUnit.TimeUnitSeconds      => JTimeUnit.SECONDS
        case TimeUnit.TimeUnitMinutes      => JTimeUnit.MINUTES
        case TimeUnit.TimeUnitHours        => JTimeUnit.HOURS
        case TimeUnit.TimeUnitDays         => JTimeUnit.DAYS
      }

      final def toBase(custom: JTimeUnit): TimeUnit = custom match {
        case JTimeUnit.NANOSECONDS  => TimeUnit.TimeUnitNanoseconds
        case JTimeUnit.MICROSECONDS => TimeUnit.TimeUnitMicroseconds
        case JTimeUnit.MILLISECONDS => TimeUnit.TimeUnitMilliseconds
        case JTimeUnit.SECONDS      => TimeUnit.TimeUnitSeconds
        case JTimeUnit.MINUTES      => TimeUnit.TimeUnitMinutes
        case JTimeUnit.HOURS        => TimeUnit.TimeUnitHours
        case JTimeUnit.DAYS         => TimeUnit.TimeUnitDays
      }
    }

}
