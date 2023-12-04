package com.improving.iam

import com.improving.config.ShowConfig
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import pdi.jwt.JwtAlgorithm

object KeyLoaderConfigSpec {

  val expectedConfig =
    KeyLoaderConfig(
      JwtAlgorithm.ES256,
      "resource:/ec_test_pub.pem",
      "resource:/ec_test_key.pem",
      Some("test")
    )

  val ecConfigObject =
    com.typesafe.config.ConfigFactory.parseString(
      """jwt-keyloader {
        |  jwt-algorithm = ES256
        |  public-key-path = "resource:/ec_test_pub.pem"
        |  private-key-path = "resource:/ec_test_key.pem"
        |  private-key-password = "test"
        |}""".stripMargin
    )

}

class KeyLoaderConfigSpec extends AnyWordSpecLike with Matchers {
  import KeyLoaderConfigSpec._

  "KeyLoaderConfig" should {

    "be able to load an instance from a configuration object" in {
      val attemptedConfig = KeyLoaderConfig.fromConfig(ecConfigObject, Some("jwt-keyloader"))
      attemptedConfig match {
        case Left(err)  => fail(err)
        case Right(cfg) => cfg mustBe expectedConfig
      }

    }

    "be able to `show` a KeyLoaderConfig instance" in {
      val instance = expectedConfig
      val actual   = ShowConfig.show(instance)

      val expectedText =
        """KeyLoaderConfig:
          |    Jwt Algorithm:        ES256
          |    Public Key Path:      resource:/ec_test_pub.pem
          |    Private Key Path:     resource:/ec_test_key.pem
          |    Private Key Password: set
          |""".stripMargin

      actual must not be empty
      actual mustBe expectedText
    }

  }

}
