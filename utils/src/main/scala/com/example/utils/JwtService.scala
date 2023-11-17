package com.example.utils

import pdi.jwt._

import scala.util._

import java.time.Duration

final class JwtService private (loader: KeyLoader, algorithm: algorithms.JwtAsymmetricAlgorithm) {

  def createAuthorizationToken(
    issuer: String,
    // subject: String,
    validFor: Duration,
    jwtId: String,
    claims: Map[String, String]
  ): Try[String] = loader.privateKey.map { privateKey =>
    val nowEpochSeconds        = SystemClock.currentInstant.toEpochMilli / 1000L
    val expirationEpochSeconds = nowEpochSeconds + validFor.toSeconds
    val claim                  = JwtClaim(
      issuer = Some(issuer),
      // subject = Some(subject),
      expiration = Some(expirationEpochSeconds),
      notBefore = Some(nowEpochSeconds),
      issuedAt = Some(nowEpochSeconds),
      jwtId = Some(jwtId)
    ) ++ (claims.toSeq: _*)

    JwtCirce.encode(claim, privateKey, algorithm)
  }

  def validateAndExtractClaim(jwt: String): Try[JwtClaim] =
    loader.publicKey.flatMap { publicKey =>
      JwtCirce.decode(jwt, publicKey, Seq(algorithm))
    }

}

object JwtService {
  final val algorithm: algorithms.JwtAsymmetricAlgorithm = JwtAlgorithm.RS256

  def apply(loader: KeyLoader): JwtService = new JwtService(loader, algorithm)
}
