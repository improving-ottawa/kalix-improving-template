package com.improving.utils.iam

import io.circe._
import io.circe.parser._
import pdi.jwt.JwtClaim

import java.time.Instant
import java.util.UUID

/** Extension to [[JwtClaim]] to include the principal's roles explicitly. */
case class AuthToken (
  jwtId: UUID,
  issuer: String,
  subject: String,
  expiration: Instant,
  notBefore: Instant,
  issuedAt: Instant,
  roles: Set[String],
  audience: Option[Set[String]],
  additionalClaims: Map[String, String]
)


/** Provides conversion to/from [[JwtClaim]] */
object AuthToken {

  /** Attempt to convert [[JwtClaim JWT claims]] into an [[AuthToken authorization token]]. */
  def fromClaim(claim: JwtClaim): Either[Throwable, AuthToken] =
    for {
      json  <- parse(claim.content)
      roles <- json.hcursor.get[Seq[String]]("roles")
      jti   <- claim.jwtId.toRight(new scala.Error("Claim has no `jti` field."))
      jwtId <- scala.util.Try(UUID.fromString(jti)).toEither
      iss   <- claim.issuer.toRight(new scala.Error("Claim has no `iss` field."))
      sub   <- claim.subject.toRight(new scala.Error("Claim has no `sub` field."))
      exp   <- claim.expiration.toRight(new scala.Error("Claim has no `exp` field."))
      nbf   <- claim.notBefore.toRight(new scala.Error("Claim has no `nbf` field."))
      iat   <- claim.issuedAt.toRight(new scala.Error("Claim has no `iat` field."))
      extra  = json.asObject.map(_.filter { case (key, _) => key == "roles" })
    } yield
      AuthToken(
        jwtId,
        iss,
        sub,
        expiration = Instant.ofEpochSecond(exp),
        notBefore = Instant.ofEpochSecond(nbf),
        issuedAt = Instant.ofEpochSecond(iat),
        roles = roles.toSet,
        audience = claim.audience,
        additionalClaims = extra.map(_.toMap.view.mapValues(Printer.noSpaces.print).toMap).getOrElse(Map.empty)
      )

  /** Converts an [[AuthToken authorization token]] into a set of [[JwtClaim JWT claims]]. */
  def toClaims(token: AuthToken): JwtClaim = {
    val args: Seq[(String, Any)] = (token.additionalClaims + ("roles" -> token.roles.toSeq)).toSeq

    JwtClaim(
      jwtId = Some(token.jwtId.toString),
      issuer = Some(token.issuer),
      subject = Some(token.subject),
      expiration = Some(token.expiration.toEpochMilli / 1000L),
      notBefore = Some(token.notBefore.toEpochMilli / 1000L),
      issuedAt = Some(token.issuedAt.toEpochMilli / 1000L),
      audience = token.audience
    ) ++ (args: _*)
  }

  implicit class ClaimExtensions(private val token: AuthToken) extends AnyVal {

    @inline final def toClaims: JwtClaim = AuthToken.toClaims(token)

  }

}

