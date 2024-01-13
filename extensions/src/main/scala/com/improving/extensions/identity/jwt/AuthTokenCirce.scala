package com.improving.extensions.identity.jwt

import io.circe.syntax._
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm
import pdi.jwt.{JwtCirceParser, JwtHeader}

import java.security.PrivateKey
import java.time.Clock

object AuthTokenCirce extends AuthTokenCirce(Clock.systemUTC) {

  def apply(clock: Clock): AuthTokenCirce = new AuthTokenCirce(clock)

}

sealed class AuthTokenCirce(override val clock: Clock) extends JwtCirceParser[JwtHeader, AuthToken] {

  final def encode(claim: AuthToken, key: PrivateKey, algorithm: JwtAsymmetricAlgorithm): String =
    encode(claim.asJson, key, algorithm)

  protected final def parseHeader(header: String): JwtHeader = {
    val cursor = parse(header).hcursor
    JwtHeader(
      algorithm = getAlg(cursor),
      typ = cursor.get[String]("typ").toOption,
      contentType = cursor.get[String]("cty").toOption,
      keyId = cursor.get[String]("kid").toOption
    )
  }

  protected final def parseClaim(claim: String): AuthToken = {
    val cursor = parse(claim).hcursor
    val authTokenResult = AuthToken.codecForAuthToken(cursor)
    authTokenResult.fold(throw _, identity)
  }

  protected final def extractExpiration(claim: AuthToken): Option[Long] =
    Some(claim.expiration.getEpochSecond)

  protected final def extractNotBefore(claim: AuthToken): Option[Long] =
    Some(claim.notBefore.getEpochSecond)

}
