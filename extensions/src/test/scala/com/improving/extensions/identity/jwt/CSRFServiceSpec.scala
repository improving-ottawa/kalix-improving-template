package com.improving.extensions.identity.jwt

import com.improving.utils._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.{Duration, Instant}
import java.util.UUID

class CSRFServiceSpec extends AnyWordSpec with Matchers {

  private val authToken = {
    val jwtId             = UUID.randomUUID()
    val fullNowInstant    = SystemClock.currentInstant
    val nowInstant        = Instant.ofEpochSecond(fullNowInstant.getEpochSecond)
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

  private val privateKey = SecureString.random(12)
  private val csrfService = CSRFService(privateKey)

  private val expectedSignatureLength = 32

  "CSRFService" should {

    "be able to generate a secure CSRF token from an `AuthToken`" in {
      val csrfToken = csrfService.createCsrfTokenForAuthToken(authToken)

      csrfToken.indexOf('.') must not be 0
      val signature = csrfToken.split('.').head
      val signatureBase64 = Base64String.unsafeFromBase64String(signature)

      signatureBase64.rawBytes must have length expectedSignatureLength
    }

    "be able to verify a CSRF token for an `AuthToken`" in {
      val csrfToken = csrfService.createCsrfTokenForAuthToken(authToken)
      val verified = csrfService.verifyCsrfTokenForAuthToken(authToken, csrfToken)

      verified match {
        case Right(()) => succeed
        case Left(err) => fail(err)
      }
    }

  }

}
