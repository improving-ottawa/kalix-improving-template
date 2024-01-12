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
  final val defaultHMACAlgorithm = "Blake3Mac"

  // While 8 bytes of entropy may not seem like a lot, this is combined with the `AuthToken` jwtID (which is a `UUID`)
  // creating a total of 128 + 64 = 192 bits of data which gets hashed for the CSRF token.
  final val defaultEntropyLength = 8

  def apply(csrfSecretKey: SecureString,
            entropyBytes: Int = CSRFService.defaultEntropyLength,
            hmacAlgorithm: String = CSRFService.defaultHMACAlgorithm
           ): CSRFService =
    new CSRFService(csrfSecretKey, entropyBytes, hmacAlgorithm)

  final case object CsrfTokenVerificationFailed extends scala.Error

  /* Internal stuff */

  private final val separator = '.'

  private final def splitToken(token: String): (String, String) = {
    @tailrec def loop(remChars: List[Char],
                      first: StringBuilder = new StringBuilder,
                      second: StringBuilder = new StringBuilder,
                      writingSecond: Boolean = false): (String, String) =
      remChars match {
        case sep :: tail if sep == separator => loop(tail, first, second, writingSecond = true)
        case head :: tail if writingSecond   => loop(tail, first, second.addOne(head), writingSecond)
        case head :: tail                    => loop(tail, first.addOne(head), second, writingSecond)
        case Nil                             => (first.toString(), second.toString())
      }

    loop(token.toList)
  }

}

/**
 * Creates CSRF tokens based on a user's [[AuthToken]] (which is a JWT).
 *
 * Based directly on guidance from OWASP "Signed Double-Submit" Cookie implemenation.
 * @see [[https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#signed-double-submit-cookie-recommended]]
 */
final class CSRFService private(csrfSecretKey: SecureString, entropyBytes: Int, hmacAlgorithm: String) {
  import CSRFService._

  private val secureRNG = new SecureRandom
  private val entropyLength = Math.max(8, entropyBytes) // No *less* than 8 bytes!
  private val hmacInstance = HMAC.create(csrfSecretKey, hmacAlgorithm)

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

    hashPart.toString + separator + messagePart
  }

  def verifyCsrfTokenForAuthToken(authToken: AuthToken, csrfToken: String): Either[Throwable, Unit] = {
    val verification =
      Try {
        val (hashPart, messagePart) = splitToken(csrfToken)
        val sessionId = ByteVector.fromUUID(authToken.jwtId)

        val csrfSignature = Base64String.unsafeFromBase64String(hashPart, urlSafe = false).rawBytes
        val csrfMessage = ByteVector(Base64String.unsafeFromBase64String(messagePart, urlSafe = false).rawBytes)

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
