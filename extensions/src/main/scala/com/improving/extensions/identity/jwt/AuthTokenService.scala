package com.improving.extensions.identity.jwt

import com.improving.extensions.identity.crypto._
import com.improving.utils.SystemClock

import cats.data.NonEmptySet
import pdi.jwt._

import java.time._
import java.util.UUID
import scala.util.Try

class AuthTokenService private[jwt](algorithmWithKeys: AlgorithmWithKeys) {

  private final val decodeAlgorithms =
    algorithmWithKeys match {
      case ECKeyPair(_, _, _)  => JwtAlgorithm.allECDSA()
      case RSAKeyPair(_, _, _) => JwtAlgorithm.allRSA()
    }

  private final val decodeJwt =
    AuthTokenCirce.decodeAll(_: String, algorithmWithKeys.publicKey, decodeAlgorithms, JwtOptions.DEFAULT).map(_._2)

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
  final def encodeToken(token: AuthToken): Either[Throwable, String] =
    Try(AuthTokenCirce.encode(token, algorithmWithKeys.privateKey, algorithmWithKeys.algorithm)).toEither

  /** Decode a JWT string into an [[AuthToken authorization token]]. */
  final def validateAndExtractToken(jwt: String): Either[Throwable, AuthToken] =
    AuthTokenCirce.decodeAll(jwt, algorithmWithKeys.publicKey, decodeAlgorithms, JwtOptions.DEFAULT)
      .map(_._2)
      .toEither

}

object AuthTokenService {

  def apply(data: AlgorithmWithKeys): AuthTokenService =
    new AuthTokenService(data)

}
