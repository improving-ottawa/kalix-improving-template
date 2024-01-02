package com.example.service3.api

import com.example.common.domain.Product
import com.example.common.Money

import com.google.protobuf.empty.Empty
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

import java.util.Currency

// This class was initially generated based on the .proto definition by Kalix tooling.

private object AllProducts {
  private final val USD = Currency.getInstance("USD")

  final val catalog = List[Product](
    // (Demo) Product 1
    Product(
      sku = "396f311a-a9bd-11ee-880f-b3d263608dff",
      name = "UltimateTech Pro Series Laptop",
      price = Money(USD, 799.99),
      shortDescription =
        """Sleek and powerful, the UltimateTech Pro Laptop combines style with high-performance computing,
          |featuring a cutting-edge Amtel i-7979M processor for efficient multitasking.""".stripMargin,
      description =
        """Introducing the UltimateTech Pro Series Laptop, a cutting-edge device designed to elevate your computing
          |experience. This sleek and powerful laptop is equipped with the latest Amtel i-7979M processor, ensuring
          |lightning-fast performance for all your tasks. Its high-resolution display provides crystal-clear visuals,
          |making it ideal for work, entertainment, and creative projects. With a durable aluminum chassis and a
          |backlit keyboard, the UltimateTech Pro Series Laptop combines style and functionality. Elevate your
          |productivity and stay connected on the go with this reliable and sophisticated laptop.""".stripMargin
    ),
    // (Demo) Product 2
    Product(
      sku = "3f3e0878-a9bd-11ee-990d-eb26afe38b85",
      name = "SmartHome Hub 2024",
      price = Money(USD, 149.99),
      shortDescription =
        """Transform your home with the SmartHome Hub 2024 – the central control unit for seamless automation of
          |lights, security, and more, all from one easy-to-use interface.""".stripMargin,
      description =
        """Transform your living space with the SmartHome Hub 2024, the central control unit for your connected home.
          |Seamlessly integrate and manage your smart devices with this intuitive hub, making home automation a breeze.
          |Compatible with popular smart home protocols, the SmartHome Hub 2024 allows you to control lighting,
          |security, climate, and more from a single interface. Its user-friendly app ensures that you have complete
          |control, whether you're at home or on the go. Upgrade your lifestyle with the SmartHome Hub 2024 – the heart
          |of your connected home.""".stripMargin
    ),
    // (Demo) Product 3
    Product(
      sku = "45a969aa-a9bd-11ee-89cc-f38075635a80",
      name = "QuantumSound Wireless Earbuds",
      price = Money(USD, 99.99),
      shortDescription =
      """Experience superior wireless audio with QuantumSound Earbuds. These sleek earbuds offer a comfortable fit,
        |touch controls, and premium sound quality for on-the-go listening pleasure.""".stripMargin,
      description =
        """Immerse yourself in premium audio with QuantumSound Wireless Earbuds. These sleek and lightweight earbuds
          |deliver an unparalleled listening experience, combining crisp highs and deep bass for a full-range sound
          |profile. With advanced Bluetooth technology, these earbuds provide a stable and seamless connection to your
          |devices. The ergonomic design ensures a comfortable fit, and the touch controls make it easy to manage calls,
          |music playback, and voice assistants. Whether you're working out, commuting, or just relaxing, QuantumSound
          |Wireless Earbuds offer the ultimate wireless audio solution. Upgrade to a new level of audio
          |excellence.""".stripMargin
    )
  )
}

class ProductsService(creationContext: ActionCreationContext) extends AbstractProductsService {

  def getProducts(empty: Empty): Action.Effect[ProductList] =
    effects.reply(ProductList(AllProducts.catalog))

  def getProductBySKU(req: SingleProductRequest): Action.Effect[Product] =
    AllProducts.catalog.find(_.sku == req.productSku)
      .map(effects.reply(_))
      .getOrElse(effects.error("No product found matching the provided SKU.", io.grpc.Status.Code.NOT_FOUND))

  def getProductsBySKU(request: MultipleProductsRequest): Action.Effect[ProductList] =
    effects.reply(
      ProductList(
        request.productSkus.foldLeft(List.empty[Product]) { case (acc, sku) =>
          AllProducts.catalog.find(_.sku == sku).fold(acc)(product => acc :+ product)
        }
      )
    )

}
