package com.example.common

import scalapb._

import java.util.Currency

trait MoneyImpl { self: Money =>

  override def toString: String = f"${currencyCode.getSymbol}$amount%,.02f"

  // Mathematical operators
  @inline final def +(other: Money): Money = Money(currencyCode, amount + other.amount)
  @inline final def -(other: Money): Money = Money(currencyCode, amount - other.amount)
  @inline final def *(other: Money): Money = Money(currencyCode, amount * other.amount)
  @inline final def /(other: Money): Money = Money(currencyCode, amount / other.amount)

  @inline final def +(amount: Double): Money = Money(currencyCode, this.amount + amount)
  @inline final def -(amount: Double): Money = Money(currencyCode, this.amount - amount)
  @inline final def *(amount: Double): Money = Money(currencyCode, this.amount * amount)
  @inline final def /(amount: Double): Money = Money(currencyCode, this.amount / amount)

}

trait MoneyCompanion extends GeneratedMessageCompanion[Money] {
  final val defaultCurrency = java.util.Currency.getInstance("USD")

  def apply(amount: BigDecimal): Money = new Money(defaultCurrency, amount)

  implicit final val orderingForMoney: Ordering[Money] =
    Ordering.by(_.amount)

  implicit final val currencyCodeMapper: TypeMapper[String, Currency] =
    new TypeMapper[String, Currency] {
      final def toCustom(base: String): Currency = Currency.getInstance(base)
      final def toBase(custom: Currency): String = custom.getCurrencyCode
    }

}
