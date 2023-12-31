package com.example.service3

import com.example.service3.api._
import com.example.service3.domain._
import com.example.service3.entity._

import kalix.scalasdk._
import org.slf4j.LoggerFactory

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

object Main {
  private val log = LoggerFactory.getLogger("com.example.service3.Main")

  lazy val kalixBuilder =
    KalixBuilder.emptyBuilder
      .autoWire[CartService]
      .autoWire[OrderNumberIssuerService]
      .autoWire[OrderNumbersService]
      .autoWire[ProductsService]
      .autoWire[ShoppingCartsService]
      .autoWire[OrderService]
      .autoWire[OrdersService]
      .autoWire[PingPong]
      .autoWire[Service3Entity]
      .registerView(new NoData3Service(_), NoData3ServiceProvider.apply)

  def createKalix(): Kalix = kalixBuilder.build

}
