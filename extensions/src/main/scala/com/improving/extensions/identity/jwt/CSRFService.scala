package com.improving.extensions.identity.jwt

import com.improving.extensions.identity.crypto.HMAC
import com.improving.utils._

import scodec.bits.ByteVector

import java.security.SecureRandom
import scala.annotation.tailrec
import scala.util.Try

object CSRFService {
  // Preferring Blake3 to SHA256.
  // See: https://peergos.org/posts/blake3
  final val defaultDigestAlgorithm = "Blake3-256"

  // While 16 bytes of entropy may not seem like a lot, this is combined with the `AuthToken` jwtID (which is a `UUID`)
  // creating a total of 128 + 128 = 256 bits of data which gets hashed for the CSRF token.
  final val defaultEntropyLength = 16

  def apply(csrfSecretKey: SecureString,
            entropyBytes: Int = CSRFService.defaultEntropyLength,
            digestAlgorithm: String = CSRFService.defaultDigestAlgorithm
           ): CSRFService =
    new CSRFService(csrfSecretKey, entropyBytes, digestAlgorithm)

  final case object CsrfTokenVerificationFailed extends scala.Error

  /* Internal stuff */

  private final val separator = '.'
}

/**
 * Creates CSRF tokens based on a user's [[AuthToken]] (which is a JWT).
 *
 * Based directly on guidance from OWASP "Signed Double-Submit" Cookie implemenation.
 * @see [[https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#signed-double-submit-cookie-recommended]]
 */
final class CSRFService private(csrfSecretKey: SecureString, entropyBytes: Int, digestAlgorithm: String) {
  import CSRFService._

  private val secureRNG = new SecureRandom
  private val entropyLength = Math.max(8, entropyBytes) // No *less* than 8 bytes!
  private val hmacInstance = HMAC.create(csrfSecretKey, digestAlgorithm)

  def createCsrfTokenForAuthToken(authToken: AuthToken): String = {
    val sessionId = ByteVector.fromUUID(authToken.jwtId)

    val entropy = new Array[Byte](entropyLength)
    secureRNG.nextBytes(entropy)

    val entropyVector = ByteVector(entropy)
    val messageBytes = (sessionId ++ entropyVector).toArray

    val hmac = hmacInstance.fold(throw _, identity)
    val hash = hmac.hash(messageBytes)

    val hashPart = Base64String(hash, urlSafe = false)
    val messagePart = Base64String(messageBytes, urlSafe = false)

    hashPart.toString + separator + messagePart.toString
  }

  def verifyCsrfTokenForAuthToken(authToken: AuthToken, csrfToken: String): Either[Throwable, Unit] = {
    val verification =
      Try {
        val tokenParts  = csrfToken.split(separator)
        val sessionId   = ByteVector.fromUUID(authToken.jwtId)
        val hashPart    = tokenParts.head
        val messagePart = tokenParts.last

        val csrfSignature = Base64String.unsafeFromBase64String(hashPart).rawBytes
        val csrfMessage = ByteVector(Base64String.unsafeFromBase64String(messagePart).rawBytes)

        val entropyVector = csrfMessage.takeRight(entropyLength)
        val messageBytes = (sessionId ++ entropyVector).toArray

        val hmac = hmacInstance.fold(throw _, identity)
        val tokenHash = hmac.hash(messageBytes)

        if (!java.util.Arrays.equals(csrfSignature, tokenHash)) {
          throw CsrfTokenVerificationFailed
        } else ()
      }

    verification
      .recoverWith { case ex: Throwable if !ex.isInstanceOf[CsrfTokenVerificationFailed.type] =>
        // Intentionally obscure any processing errors for security purposes
        scala.util.Failure(CsrfTokenVerificationFailed)
      }
      .toEither
  }

}
