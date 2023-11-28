package com.improving.utils.iam

import com.example.utils.SystemClock

import java.time._
import java.util.UUID

trait JwtService {

  /** Create a new Json Web Token (JWT) for application authorization. */
  def createToken(
    issuer: String,
    subject: String,
    validFor: Duration,
    principalRoles: Set[String],
    additionalClaims: Map[String, String] = Map.empty,
    overrideJwtId: Option[String] = None
  ): Either[Throwable, String] = {
    val nowEpochSeconds = SystemClock.currentInstant.toEpochMilli / 1000L
    val expirationEpochSeconds = nowEpochSeconds + validFor.toSeconds

    encodeToken(
      AuthToken(
        jwtId = overrideJwtId.map(UUID.fromString).getOrElse(UUID.randomUUID()),
        issuer,
        subject,
        expiration = Instant.ofEpochSecond(expirationEpochSeconds),
        notBefore = Instant.ofEpochSecond(nowEpochSeconds),
        issuedAt = Instant.ofEpochSecond(nowEpochSeconds),
        roles = principalRoles,
        audience = None,
        additionalClaims = additionalClaims
      )
    )
  }

  def encodeToken(token: AuthToken): Either[Throwable, String]

  def validateAndExtractToken(jwt: String): Either[Throwable, AuthToken]

}
