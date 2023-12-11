package com.improving.extensions.oidc

import com.improving.utils.SecureRandomString
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OIDCStateSpec extends AnyWordSpec with Matchers {

  "OIDCState" should {

    "be serializable into a byte array" in {
      val providerId  = "Google"
      val nonce       = SecureRandomString(16)
      val redirectUrl = "http://localhost:9000/"
      val state       = OIDCState(providerId, redirectUrl, nonce)
      val byteArray   = OIDCState.toByteArray(state)
      byteArray.length must be > 0
    }

    "be serializable into a byte array (empty strings)" in {
      val state     = OIDCState("Google", "")
      val byteArray = OIDCState.toByteArray(state)
      byteArray.length must be > 0
    }

    "be deserializable from a byte array" in {
      val providerId  = "Google"
      val nonce       = SecureRandomString(32)
      val redirectUrl = "http://localhost:9000/"
      val expected    = OIDCState(providerId, redirectUrl, nonce)
      val byteArray   = OIDCState.toByteArray(expected)
      val actual      = OIDCState.fromByteArray(byteArray)

      actual mustBe expected
    }

  }

}
