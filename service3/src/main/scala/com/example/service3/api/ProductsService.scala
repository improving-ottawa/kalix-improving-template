package com.example.service3.api

import com.example.common.domain.Product
import com.google.protobuf.empty.Empty
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

// This class was initially generated based on the .proto definition by Kalix tooling.

private object AllProducts {
  // TODO: Populate with products
}

class ProductsService(creationContext: ActionCreationContext) extends AbstractProductsService {

  override def getProducts(empty: Empty): Action.Effect[ProductList] =
    effects.reply(ProductList(List.empty))

  override def getProductBySKU(productSKURequest: ProductSKURequest): Action.Effect[Product] = {
    throw new RuntimeException("The command handler for `GetProductBySKU` is not implemented, yet")
  }

}
