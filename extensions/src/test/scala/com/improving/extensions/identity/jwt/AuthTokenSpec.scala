package com.improving.extensions.identity.jwt

import com.improving.utils.SystemClock
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time._
import java.util.UUID

class AuthTokenSpec extends AnyWordSpecLike with Matchers {

  private val jwtId             = UUID.randomUUID()
  private val fullNowInstant    = SystemClock.currentInstant
  private val nowInstant        = Instant.ofEpochSecond(fullNowInstant.getEpochSecond)
  private val expirationInstant = nowInstant.plus(Duration.ofMinutes(90))

  private val testToken1 = AuthToken(
    jwtId,
    "test.org",
    "test.user@test.org",
    expirationInstant,
    nowInstant,
    nowInstant,
    Set("testing")
  )

  private val testToken2 = AuthToken(
    jwtId,
    "test.org",
    "test.user@test.org",
    expirationInstant,
    nowInstant,
    nowInstant,
    Set("testing"),
    Set("can-test"),
    Some(Set("audience"))
  )

  private val testToken3 = AuthToken(
    jwtId,
    "test.org",
    "test.user@test.org",
    expirationInstant,
    nowInstant,
    nowInstant,
    Set("testing"),
    Set("can-test"),
    Some(Set("audience")),
    Map(
      "key1" -> "value1",
      "key2" -> "value2",
    )
  )

  private val codec = AuthToken.codecForAuthToken

  "AuthToken" should {

    "be convertable to/from JSON" in {
      val json1 = codec(testToken1)
      json1.asObject.isDefined mustBe true

      val fromJson1 = codec(json1.hcursor)
      fromJson1.fold(fail(_), at => at mustBe testToken1)
    }

    "be convertable to/from JSON (with permissions)" in {
      val json2 = codec(testToken2)
      json2.asObject.isDefined mustBe true

      val fromJson2 = codec(json2.hcursor)
      fromJson2.fold(fail(_), at => at mustBe testToken2)
    }

    "be convertable to/from JSON (with additional claims)" in {
      val json3 = codec(testToken3)
      json3.asObject.isDefined mustBe true

      val fromJson3 = codec(json3.hcursor)
      fromJson3.fold(fail(_), at => at mustBe testToken3)
    }

  }

}
