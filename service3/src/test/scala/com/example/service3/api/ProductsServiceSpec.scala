package com.example.service3.api

import com.example.common.domain.Product
import com.google.protobuf.empty.Empty
import kalix.scalasdk.action.Action
import kalix.scalasdk.testkit.ActionResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class ProductsServiceSpec extends AnyWordSpec with Matchers {

  "ProductsService" must {

    "have example test that can be removed" in {
      val service = ProductsServiceTestKit(new ProductsService(_))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // result.reply shouldBe expectedReply
    }

    "handle command GetProducts" in {
      val service = ProductsServiceTestKit(new ProductsService(_))
      pending
      // val result = service.getProducts(Empty(...))
    }

    "handle command GetProductBySKU" in {
      val service = ProductsServiceTestKit(new ProductsService(_))
      pending
      // val result = service.getProductBySKU(SingleProductRequest(...))
    }

    "handle command GetProductsBySKU" in {
      val service = ProductsServiceTestKit(new ProductsService(_))
      pending
      // val result = service.getProductsBySKU(MultipleProductsRequest(...))
    }

  }
}
