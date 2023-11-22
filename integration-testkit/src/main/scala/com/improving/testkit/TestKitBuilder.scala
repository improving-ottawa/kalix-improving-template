package com.improving.testkit

trait TestKitBuilder { self: internal.TestKitImpl =>

  /** Register a [[KalixService Kalix service]] which will be instantiated and managed by the TestKit. */
  def withKalixService(service: KalixService): BuildableTestKitBuilder

}

trait BuildableTestKitBuilder extends TestKitBuilder { self: internal.TestKitImpl =>

  /** Builds the [[IntegrationTestKitApi IntegrationTestKit]] based on the registered Kalix services. */
  def buildKit: IntegrationTestKitApi

}
