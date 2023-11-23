package com.example.utils.iam.jwt

import akka.http.scaladsl.model.DateTime

import java.util.UUID
import cats.effect._
import cats.implicits._
import com.example.utils.iam.model._
import com.example.utils.iam.model.claims._
import io.circe._
import io.circe.syntax._
import pdi.jwt.JwtBase64

import java.time.{Duration, Instant}

/**
  * A Json Web Token (JWT) containing some payload.
  */
final case class JsonWebToken[P <: IamPayload] private (
  payload: P,
  issuer: String,
  audience: Set[String],
  expiration: Instant,
  issuedAt: Instant,
  notBefore: Option[Instant]
) {

  def jwtId: UUID   = JsonWebToken.getJwtId(payload)
  def subject: UUID = JsonWebToken.getSubject(payload)

  /** Validates this Jwt using the default configured validators. */
  def validateDefaults: JwtValidation.ValidationResult[P] = JwtValidation.validateDefault(this)

  /** Simply returns true/false based on whether this Jwt is expired or not. */
  def isExpired: Boolean = JwtValidation.isExpired(this)
}

object JsonWebToken extends ClaimCodecHelpers {
  final val defaultLeeway = Duration.ofSeconds(15)

  def apply[P <: IamPayload](
    payload: P,
    issuer: String,
    tokenLifetime: Duration,
    audience: Set[String] = Set.empty,
    leeway: Option[Duration] = Some(defaultLeeway)
  ): JsonWebToken[P] = {
    require(tokenLifetime != Duration.ZERO, "Tokens cannot have a zero duration lifetime.")
    require(leeway.forall(_.toMillis > 0), "Tokens cannot have a negative leeway.")

    val nowUtc     = Instant.now()
    val expiration = nowUtc.plus(tokenLifetime)
    val notBefore  = leeway.map(nowUtc.minus)

    JsonWebToken(payload, issuer, audience, expiration, nowUtc, notBefore)
  }

  def decode[P <: IamPayload](rawToken: String)(implicit decoder: Decoder[P]): IO[JsonWebToken[P]] = {
    val tokenParts = rawToken.split('.').toList

    for {
      tokenBody <- IO(JwtBase64.decodeString(tokenParts(1)))
      jsonBody  <- IO.fromEither(parser.parse(tokenBody))
      jwt       <- IO.fromEither(fromJson[P].decodeJson(jsonBody))
    } yield jwt
  }

  implicit final def toJson[P <: IamPayload](implicit encoder: Encoder[P]): Encoder[JsonWebToken[P]] =
    new Encoder[JsonWebToken[P]] {

      final def apply(jwtToken: JsonWebToken[P]) = {
        val fields: Seq[(String, Json)] = Seq(
          "iss" -> jwtToken.issuer.asJson,
          "sub" -> jwtToken.subject.asJson,
          "exp" -> writeSeconds(jwtToken.expiration),
          "iat" -> writeSeconds(jwtToken.issuedAt),
          "typ" -> getType(jwtToken.payload).asJson,
          "jti" -> jwtToken.jwtId.asJson
        ) ++
          jwtToken.notBefore.map(nbf => "nbf" -> writeSeconds(nbf)).toSeq ++
          encodeAudience(jwtToken.audience)

        val claimJson = encoder(jwtToken.payload)

        Json.obj(fields: _*).deepMerge(claimJson)
      }

      @inline final private def encodeAudience(audience: Set[String]): Seq[(String, Json)] =
        if (audience.size == 1) List("aud" -> audience.head.asJson)
        else if (audience.isEmpty) List()
        else List("aud" -> audience.asJson)

    }

  implicit final def fromJson[C <: IamPayload](implicit decoder: Decoder[C]): Decoder[JsonWebToken[C]] =
    new Decoder[JsonWebToken[C]] {

      override def apply(c: HCursor) = for {
        fields <- decodeFields(c)
        claim  <- decoder(c)
      } yield {
        val (iss, aud, exp, iat, nbf) = fields
        JsonWebToken(claim, iss, aud, exp, iat, nbf)
      }

      @inline final private def decodeFields(c: HCursor) =
        for {
          iss <- c.get[String]("iss")
          aud <- decodeAudience(c)
          exp <- readSeconds(c)("exp")
          iat <- readSeconds(c)("iat")
          nbf <- readSecondsOption(c)("nbf")
        } yield (iss, aud, exp, iat, nbf)

      @inline final private def decodeAudience(c: HCursor) =
        c.get[Set[String]]("aud").handleErrorWith { _ =>
          c.get[Option[String]]("aud").map(_.toSet)
        }

    }

  /**
    * Allows for treating/accessing a [[JsonWebToken]] `JwtToken[C]` as an instance of `C`.
    */
  object ImplicitClaim {
    import scala.language.implicitConversions
    implicit final def jwtTokenToClaim[C <: IamPayload](jwtToken: JsonWebToken[C]): C = jwtToken.payload
  }

  // Functions which extracts mandatory fields for AMPayload types
  private def getSubject(payload: IamPayload) = payload match {
    case claim: IamClaim         => claim.principal.id
    case principal: IamPrincipal => principal.id
  }

  private def getJwtId(payload: IamPayload) = payload match {
    case claim: IamClaim => claim.id
    case _: IamPrincipal => UUID.randomUUID()
  }

  private def getType(payload: IamPayload) = payload match {
    case _: AccessClaim   => "Bearer"
    case _: RefreshClaim  => "Refresh"
    case _: IdentityClaim => "ID"
    case _                => "Token"
  }

}
