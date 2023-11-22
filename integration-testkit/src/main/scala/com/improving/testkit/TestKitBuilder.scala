package com.improving.testkit

/**
  * A "builder" for creating a [[IntegrationTestKit TestKit]], which requires you register at least one
  * [[KalixService Kalix service]] __first__ before you can build the kit.
  */
trait TestKitBuilder { self: internal.TestKitImpl =>

  /** Register a [[KalixService Kalix service]] which will be instantiated and managed by the TestKit. */
  def withKalixService(service: KalixService): BuildableTestKitBuilder

}

/**
  * A "builder" for creating a [[IntegrationTestKit TestKit]], which has at least one [[KalixService Kalix service]]
  * registered, and hence can be instantiated via the `buildKit` function.
  */
trait BuildableTestKitBuilder extends TestKitBuilder { self: internal.TestKitImpl =>

  /** Builds the [[IntegrationTestKit IntegrationTestKit]] based on the registered Kalix services. */
  def buildKit: IntegrationTestKit

}
