package com.improving.iam

import com.example.utils.SystemClock
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time._
import java.util.UUID

class AuthTokenSpec extends AnyWordSpecLike with Matchers {

  "AuthToken" should {

    "be convertable to `JwtClaim` and back to `AuthToken`" in {
      val jwtId             = UUID.randomUUID()
      val fullNowInstant    = SystemClock.currentInstant
      val nowInstant        = Instant.ofEpochSecond(fullNowInstant.getEpochSecond)
      val expirationInstant = nowInstant.plus(Duration.ofMinutes(90))
      val expectedToken     =
        AuthToken(
          jwtId,
          "test.org",
          "test.user@test.org",
          expirationInstant,
          nowInstant,
          nowInstant,
          Set("testing")
        )

      val jwtClaim     = AuthToken.toClaims(expectedToken)
      val tokenAttempt = AuthToken.fromClaim(jwtClaim)

      tokenAttempt match {
        case Left(error)  => fail(error)
        case Right(token) => token mustBe expectedToken
      }
    }

  }

}
