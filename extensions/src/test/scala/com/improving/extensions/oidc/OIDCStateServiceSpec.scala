package com.improving.extensions.oidc

import com.improving.iam.{KeyLoader, KeyLoaderConfig}
import com.improving.utils.SecureRandomString
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pdi.jwt.JwtAlgorithm

object OIDCStateServiceSpec {

  private val ecConfig =
    KeyLoaderConfig(
      JwtAlgorithm.ES256,
      "resource:/ec_test_pub.pem",
      "resource:/ec_test_key.pem",
      Some("test")
    )

  private val expected =
    OIDCState(
      "Google",
      SecureRandomString(16).toString,
      "http://localhost:9000/"
    )

}

class OIDCStateServiceSpec extends AnyWordSpec with Matchers {
  import OIDCStateServiceSpec._

  private val algorithmWithKeys = KeyLoader.load(ecConfig).fold(throw _, identity)
  private val service = OIDCStateService(algorithmWithKeys)

  "OIDCStateService" should {

    "be able to sign and encode an OIDCState into a token" in {
      val token = service.signToken(expected)
      token.indexOf('.') must not be 0
    }

    "be able to decode a signed state token into an OIDCState instance" in {
      val token = service.signToken(expected)
      val result = service.parseSessionToken(token)
      result match {
        case Left(error)   => fail(error)
        case Right(actual) => actual mustBe expected
      }
    }

  }

}
