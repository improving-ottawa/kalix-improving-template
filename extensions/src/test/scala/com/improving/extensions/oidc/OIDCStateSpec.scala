package com.improving.extensions.oidc

import com.improving.utils.SecureRandomString
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OIDCStateSpec extends AnyWordSpec with Matchers {

  "OIDCSession" should {

    "be serializable into a byte array" in {
      val providerId  = "Google"
      val csrfToken   = SecureRandomString(32)
      val redirectUrl = "http://localhost:9000/"
      val state       = OIDCState(providerId, csrfToken.toString, redirectUrl)
      val byteArray   = OIDCState.toByteArray(state)
      byteArray.length must be > 0
    }

    "be serializable into a byte array (empty strings)" in {
      val state     = OIDCState("Google", "", "")
      val byteArray = OIDCState.toByteArray(state)
      byteArray.length must be > 0
    }

    "be deserializable from a byte array" in {
      val providerId  = "Google"
      val csrfToken   = SecureRandomString(32)
      val redirectUrl = "http://localhost:9000/"
      val expected    = OIDCState(providerId, csrfToken.toString, redirectUrl)
      val byteArray   = OIDCState.toByteArray(expected)
      val actual      = OIDCState.fromByteArray(byteArray)

      actual mustBe expected
    }

  }

}
