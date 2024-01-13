package com.improving.extensions.identity.jwt

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import pdi.jwt.JwtClaim

import java.time.Instant
import java.util.UUID

/** Extension to [[pdi.jwt.JwtClaim]] that include the principal's roles and explicit permissions. */
case class AuthToken(
  jwtId: UUID,
  issuer: String,
  subject: String,
  expiration: Instant,
  notBefore: Instant,
  issuedAt: Instant,
  roles: Set[String],
  explicitPermissions: Set[String],
  audience: Option[Set[String]],
  additionalClaims: Map[String, String]
)

/** Provides conversion to/from [[pdi.jwt.JwtClaim]] */
object AuthToken {

  /** [[AuthToken]] constructor with default values. */
  def apply(
    jwtId: UUID,
    issuer: String,
    subject: String,
    expiration: Instant,
    notBefore: Instant,
    issuedAt: Instant,
    roles: Set[String],
    explicitPermissions: Set[String] = Set.empty,
    audience: Option[Set[String]] = None,
    additionalClaims: Map[String, String] = Map.empty
  ): AuthToken =
    new AuthToken(
      jwtId,
      issuer,
      subject,
      expiration,
      notBefore,
      issuedAt,
      roles,
      explicitPermissions,
      audience,
      additionalClaims
    )

  implicit final val codecForAuthToken: Codec[AuthToken] = new Codec[AuthToken] {
    private final val definedKeys = List(
      "jti",
      "iss",
      "sub",
      "exp",
      "nbf",
      "iat",
      "aud",
      "roles",
      "permissions"
    )

    private final def parseAdditionalClaims(cursor: HCursor) = {
      val updated =
        definedKeys.foldLeft(cursor) { (cursor, field) =>
          cursor.downField(field).delete.success match {
            case Some(newCursor) => newCursor
            case None            => cursor
          }
        }

      @inline def jsonValueToString(json: Json): String =
        json.asString.fold("")(identity)

      updated.top
        .flatMap(_.asObject)
        .map(obj => obj.toMap.view.mapValues(jsonValueToString).toMap)
        .getOrElse(Map.empty)
    }

    def apply(c: HCursor): Decoder.Result[AuthToken] =
      for {
        jti <- c.get[UUID]("jti")
        iss <- c.get[String]("iss")
        sub <- c.get[String]("sub")
        exp <- c.get[Long]("exp").map(Instant.ofEpochSecond)
        nbf <- c.get[Long]("nbf").map(Instant.ofEpochSecond)
        iat <- c.get[Long]("iat").map(Instant.ofEpochSecond)
        aud <- c.get[Option[Seq[String]]]("aud").map(_.map(_.toSet))
        roles <- c.get[Seq[String]]("roles").map(_.toSet)
        perms <- c.get[Seq[String]]("permissions").map(_.toSet)
      } yield AuthToken(
        jwtId = jti,
        issuer = iss,
        subject = sub,
        expiration = exp,
        notBefore = nbf,
        issuedAt = iat,
        audience = aud,
        roles = roles,
        explicitPermissions = perms,
        additionalClaims = parseAdditionalClaims(c)
      )

    final def apply(a: AuthToken): Json = {
      val fixedFields = Map(
        "jti" -> a.jwtId.asJson,
        "iss" -> a.issuer.asJson,
        "sub" -> a.subject.asJson,
        "exp" -> a.expiration.getEpochSecond.asJson,
        "nbf" -> a.notBefore.getEpochSecond.asJson,
        "iat" -> a.issuedAt.getEpochSecond.asJson,
        "aud" -> a.audience.asJson,
        "roles" -> a.roles.asJson,
        "permissions" -> a.explicitPermissions.asJson,
      )

      val allFields = (
        fixedFields ++
        a.additionalClaims.map { case (key, value) => key -> Json.fromString(value) }
      ).toSeq

      Json.obj(allFields: _*)
    }
  }

  /** Attempt to convert [[JwtClaim JWT claims]] into an [[AuthToken authorization token]]. */
  def fromClaim(claim: JwtClaim): Either[Throwable, AuthToken] =
    for {
      json  <- parse(claim.content)
      roles <- json.hcursor.get[Seq[String]]("roles")
      perms <- json.hcursor.get[Option[Seq[String]]]("permissions")

      jti   <- claim.jwtId.toRight(new scala.Error("Claim has no `jti` field."))
      jwtId <- scala.util.Try(UUID.fromString(jti)).toEither

      iss   <- claim.issuer.toRight(new scala.Error("Claim has no `iss` field."))
      sub   <- claim.subject.toRight(new scala.Error("Claim has no `sub` field."))
      exp   <- claim.expiration.toRight(new scala.Error("Claim has no `exp` field."))
      nbf   <- claim.notBefore.toRight(new scala.Error("Claim has no `nbf` field."))
      iat   <- claim.issuedAt.toRight(new scala.Error("Claim has no `iat` field."))
      extra  = json.asObject.map(_.filter { case (key, _) => key != "roles" })
    } yield AuthToken(
      jwtId,
      iss,
      sub,
      expiration = Instant.ofEpochSecond(exp),
      notBefore = Instant.ofEpochSecond(nbf),
      issuedAt = Instant.ofEpochSecond(iat),
      roles = roles.toSet,
      explicitPermissions = perms.map(_.toSet).getOrElse(Set.empty),
      audience = claim.audience,
      additionalClaims = extra.map(_.toMap.view.mapValues(printJsonValue).toMap).getOrElse(Map.empty),
    )

  /** Converts an [[AuthToken authorization token]] into a set of [[JwtClaim JWT claims]]. */
  def toClaims(token: AuthToken): JwtClaim = {
    val args: Seq[(String, Any)] = (
      token.additionalClaims +
      ("roles" -> token.roles.toSeq) +
      ("permissions" -> token.explicitPermissions.toSeq)
    ).toSeq

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

  @inline final private def printJsonValue(value: Json): String =
    value.noSpaces.stripPrefix("\"").stripSuffix("\"")

}
