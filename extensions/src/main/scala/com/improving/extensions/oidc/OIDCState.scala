package com.improving.extensions.oidc

import com.improving.iam.AlgorithmWithKeys
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.improving.utils.{Base64String, SystemClock}
import pdi.jwt.JwtUtils

import java.time.Instant
import scala.annotation.switch
import scala.util.Try

case class OIDCState(
  providerId: String,
  issuedAt: Instant,
  csrfToken: String,
  redirectUri: String,
)

object OIDCState {

  def apply(providerId: String, csrfToken: String, redirectUri: String): OIDCState =
    new OIDCState(providerId, SystemClock.currentInstant, csrfToken, redirectUri)

  def toByteArray(session: OIDCState): Array[Byte] = {
    val OIDCState(providerId, issuedAt, csrfToken, redirectUri) = session

    val size = {
      val s1 = if (providerId.nonEmpty) CodedOutputStream.computeStringSize(1, providerId) else 0
      val s2 = CodedOutputStream.computeInt64Size(2, issuedAt.getEpochSecond)
      val s3 = CodedOutputStream.computeInt32Size(3, issuedAt.getNano)
      val s4 = if (csrfToken.nonEmpty) CodedOutputStream.computeStringSize(4, csrfToken) else 0
      val s5 = if (redirectUri.nonEmpty) CodedOutputStream.computeStringSize(5, redirectUri) else 0
      s1 + s2 + s3 + s4 + s5
    }

    val a            = new Array[Byte](size)
    val outputStream = CodedOutputStream.newInstance(a)

    if (providerId.nonEmpty) outputStream.writeString(1, providerId)
    outputStream.writeInt64(2, issuedAt.getEpochSecond)
    outputStream.writeInt32(3, issuedAt.getNano)
    if (csrfToken.nonEmpty) outputStream.writeString(4, csrfToken)
    if (redirectUri.nonEmpty) outputStream.writeString(5, redirectUri)

    outputStream.checkNoSpaceLeft()
    a
  }

  def fromByteArray(array: Array[Byte]): OIDCState = {
    var providerId  = ""
    var epochSecond = 0L
    var nano        = 0
    var csrfToken   = ""
    var redirectUri = ""
    var done        = false

    val input = CodedInputStream.newInstance(array)
    while (!done) {
      val tag = input.readTag()
      (tag: @switch) match {
        case 0  => done = true
        case 10 => providerId = input.readStringRequireUtf8()
        case 16 => epochSecond = input.readInt64()
        case 24 => nano = input.readInt32()
        case 34 => csrfToken = input.readStringRequireUtf8()
        case 42 => redirectUri = input.readStringRequireUtf8()
      }
    }

    OIDCState(providerId, Instant.ofEpochSecond(epochSecond, nano), csrfToken, redirectUri)
  }

}

object OIDCStateService {

  def apply(algorithmWithKeys: AlgorithmWithKeys): OIDCStateService =
    new OIDCStateService(algorithmWithKeys)

  case class InvalidStateToken(received: String)
      extends Error(s"Invalid OIDC state received (expected two base64 string separated by a '.'): $received")

  case class SignatureVerificationFailed(received: String)
      extends Error(s"Invalid cryptographic signature in received state token: $received")

}

final class OIDCStateService private (algorithmWithKeys: AlgorithmWithKeys) {
  import OIDCStateService._

  def signToken(session: OIDCState): String = {
    val data      = OIDCState.toByteArray(session)
    val signature = JwtUtils.sign(data, algorithmWithKeys.privateKey, algorithmWithKeys.algorithm)

    val header = Base64String(signature)
    val body   = Base64String(data)

    header + "." + body
  }

  def parseSessionToken(token: String): Either[Throwable, OIDCState] = {
    val headerAndBody = Try {
      val parts = token.split('.')
      if (parts.length != 2) throw InvalidStateToken(token)
      (parts.head, parts.last)
    }

    @inline def validateSignature(header: Base64String, body: Base64String): Either[Throwable, Unit] = {
      if (JwtUtils.verify(body.rawBytes, header.rawBytes, algorithmWithKeys.publicKey, algorithmWithKeys.algorithm))
        Right(())
      else
        Left(SignatureVerificationFailed(token))
    }

    for {
      (head, body) <- headerAndBody.toEither
      header       <- Base64String.fromBase64String(head)
      data         <- Base64String.fromBase64String(body)
      _            <- validateSignature(header, data)
      result       <- Try(OIDCState.fromByteArray(data.rawBytes)).toEither
    } yield result
  }

}
