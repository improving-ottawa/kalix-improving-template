package com.improving.iam

import com.example.utils.SystemClock
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time._
import java.util.UUID

class AuthTokenServiceSpec extends AnyWordSpec with Matchers {

  private lazy val algoWithKeys = KeyLoader.load(KeyLoaderSpec.ecConfig).fold(throw _, identity)

  private val authToken = {
    val jwtId = UUID.randomUUID()
    val fullNowInstant = SystemClock.currentInstant
    val nowInstant = Instant.ofEpochSecond(fullNowInstant.getEpochSecond)
    val expirationInstant = nowInstant.plus(Duration.ofMinutes(90))
    AuthToken(
      jwtId,
      "test.org",
      "test.user@test.org",
      expirationInstant,
      nowInstant,
      nowInstant,
      Set("testing")
    )
  }

  "AuthTokenService" should {

    "be able to encode a token into a JWT" in {
      val service = AuthTokenService(algoWithKeys)
      val attemptJwt = service.encodeToken(authToken)
      attemptJwt match {
        case Left(error)  => fail(error)
        case Right(token) => token.split('.').length mustBe 3
      }
    }

    "be able to decode a JWT string into an authorization token" in {
      val service = AuthTokenService(algoWithKeys)
      val jwt = service.encodeToken(authToken).fold(throw _, identity)

      val attemptToken = service.validateAndExtractToken(jwt)
      attemptToken match {
        case Left(error)  => fail(error)
        case Right(token) => token mustBe authToken
      }
    }

  }

}
