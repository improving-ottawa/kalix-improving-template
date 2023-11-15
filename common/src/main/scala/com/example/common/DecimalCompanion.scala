package com.example.common

import scalapb._

import scala.language.implicitConversions

trait DecimalCompanion extends GeneratedMessageCompanion[Decimal] {

  implicit final val decimalToBigDecimalTypeMapper: TypeMapper[Decimal, BigDecimal] =
    TypeMapper[Decimal, BigDecimal](
      decimal => BigDecimal(BigInt(decimal.unscaledValue.toByteArray), decimal.scale))(
      scala => {
        val unscaled = com.google.protobuf.ByteString.copyFrom(scala.underlying.unscaledValue.toByteArray)
        Decimal(unscaled, scala.scale)
      }
    )

  implicit final def toScala(decimal: Decimal): BigDecimal = decimalToBigDecimalTypeMapper.toCustom(decimal)
  implicit final def toProto(bigDecimal: BigDecimal): Decimal = decimalToBigDecimalTypeMapper.toBase(bigDecimal)

}
