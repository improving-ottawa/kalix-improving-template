package com.improving.extensions.identity.jwt

import com.improving.extensions.identity.crypto.AlgorithmWithKeys
import com.improving.utils.SystemClock

import cats.data.NonEmptySet
import pdi.jwt.JwtCirce

import java.time._
import java.util.UUID
import scala.util.Try

final class AuthTokenService private(cryptoData: AlgorithmWithKeys) {

  /**
    * Create a new Json Web Token (JWT) string from an [[AuthToken authorization token]] for application authorization.
    */
  def createToken(
    issuer: String,
    subject: String,
    validFor: Duration,
    principalRoles: NonEmptySet[String],
    explicitPermissions: Set[String] = Set.empty,
    additionalClaims: Map[String, String] = Map.empty,
    overrideJwtId: Option[UUID] = None
  ): Either[Throwable, String] = {
    val nowInstant = SystemClock.currentInstant
    val expiresOn  = nowInstant.plus(validFor)
    val jwtId      = overrideJwtId.getOrElse(UUID.randomUUID)

    encodeToken(
      AuthToken(
        jwtId,
        issuer,
        subject,
        expiration = expiresOn,
        notBefore = nowInstant,
        issuedAt = nowInstant,
        roles = principalRoles.toSortedSet,
        explicitPermissions = explicitPermissions,
        audience = None,
        additionalClaims = additionalClaims
      )
    )
  }

  /** Encode an [[AuthToken authorization token]] into a JWT string. */
  def encodeToken(token: AuthToken): Either[Throwable, String] =
    Try(JwtCirce.encode(token.toClaims, cryptoData.privateKey, cryptoData.algorithm)).toEither

  /** Decode a JWT string into an [[AuthToken authorization token]]. */
  def validateAndExtractToken(jwt: String): Either[Throwable, AuthToken] =
    for {
      claim <- JwtCirce.decode(jwt, cryptoData.publicKey).toEither
      token <- AuthToken.fromClaim(claim)
    } yield token

}

object AuthTokenService {

  def apply(data: AlgorithmWithKeys): AuthTokenService =
    new AuthTokenService(data)

}
