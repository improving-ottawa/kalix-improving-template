package com.example.gateway.api

import com.example.common.domain.Product
import com.example.gateway.middleware._
import com.example.service3.api._

import com.google.protobuf.empty.Empty
import kalix.scalasdk.action.Action

trait ProductsProxy extends GatewayProxyBase with AuthenticatedAction with AuthorizedAction {

  def getProducts(empty: Empty): Action.Effect[ProductList] =
    authenticatedEffect(_ =>
      effects.asyncReply(productsService.getProducts(empty))
    )

  def getProductBySKU(request: SingleProductRequest): Action.Effect[Product] =
    authenticatedEffect(_ =>
      effects.asyncReply(productsService.getProductBySKU(request))
    )

  def getProductsBySKU(request: MultipleProductsRequest): Action.Effect[ProductList] =
    authenticatedEffect(_ =>
      effects.asyncReply(productsService.getProductsBySKU(request))
    )

}
