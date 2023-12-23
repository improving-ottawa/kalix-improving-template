package com.example.service3

import kalix.scalasdk._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class KalixBuilderSpec extends AnyWordSpec with Matchers {

  "KalixBuilder" should {
    "should create a valid Kalix instance" in {
      val kalixBuilder  = Main.kalixBuilder
      val kalixInstance = kalixBuilder.build

      WrappedKalix(kalixInstance).numberOfRegistrations mustBe kalixBuilder.numberOfRegistrations
    }
  }

}
