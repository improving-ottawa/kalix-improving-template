package com.improving.extensions.identity.crypto

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pdi.jwt.JwtAlgorithm

object KeyLoaderSpec {

  private[identity] val ecConfig =
    KeyLoaderConfig(
      JwtAlgorithm.ES256,
      "resource:/ec_test_pub.pem",
      "resource:/ec_test_key.pem",
      Some("test")
    )

  private val rsaConfig =
    KeyLoaderConfig(
      JwtAlgorithm.RS256,
      "resource:/rsa_test_pub.pem",
      "resource:/rsa_test_key.pem",
      Some("test")
    )

}

class KeyLoaderSpec extends AnyWordSpec with Matchers {
  import KeyLoaderSpec._

  "KeyLoader" should {

    "be able to load a ECKeyPair" in {
      val result = KeyLoader.load(ecConfig)
      result match {
        case Left(errs) => fail(errs)
        case Right(res) => res.algorithm == ecConfig.jwtAlgorithm
      }
    }

    "be able to load a RSAKeyPair" in {
      val result = KeyLoader.load(rsaConfig)
      result match {
        case Left(error) => fail(error)
        case Right(res)  => res.algorithm == rsaConfig.jwtAlgorithm
      }
    }

  }

}
