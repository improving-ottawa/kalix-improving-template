package com.example.service3

import com.example.service3.api._
import com.example.service3.domain._
import com.example.service3.entity.Service3Entity
import kalix.scalasdk.Kalix
import org.slf4j.LoggerFactory

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

object Main {

  private val log = LoggerFactory.getLogger("com.example.service3.Main")

  def createKalix(): Kalix = {
    // The KalixFactory automatically registers any generated Actions, Views or Entities,
    // and is kept up-to-date with any changes in your protobuf definitions.
    // If you prefer, you may remove this and manually register these components in a
    // `Kalix()` instance.
    KalixFactory.withComponents(
      new CartService(_),
      new OrderNumberIssuerService(_),
      new OrderService(_),
      new Service3Entity(_),
      new NoData3Service(_),
      new OrderNumbersService(_),
      new OrdersService(_),
      new PingPong(_),
      new ProductsService(_),
      new ShoppingCartsService(_)
    )
  }

}
