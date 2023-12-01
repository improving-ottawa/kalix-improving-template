package com.improving.extensions.oidc

import cats.syntax.all._
import io.circe._
import io.circe.syntax._

import java.util.UUID

/**
  * An OpenID Connect user principal.
  *
  * @note
  *   This is an amalgamation of the following OIDC scopes: `openid`, `profile`, `email`, `roles`. The "sub" (subject)
  *   field is expected to contain a UUID, which is the user's unique identifier.
  *
  * @note
  *   If the subject is not a unique identifier, __THEN A NEW UUID IS GENERATED FOR THIS USER__ using the subject
  *   as entropy. If the provider ever changes the subject for this principal (very unlikely) then this will link to a new
  *   user principal.
  *
  * @param id
  *   The unique identifier for this user principal
  * @param name
  *   The user's full displayable name as defined by the OIDC Standard
  * @param preferredName
  *   Shorthand name by which the End-User wishes to be referred to at the RP, such as "jane.doe"
  * @param familyName
  *   The user's `family_name`
  * @param givenName
  *   The user's `given_name`
  * @param middleName
  *   The user's `middle_name`
  * @param email
  *   The user's `email` address (WHICH MIGHT NOT BE POPULATED!)
  */
case class OIDCIdentity(
  id: UUID,
  name: String,
  preferredName: Option[String],
  familyName: Option[String],
  givenName: Option[String],
  middleName: Option[String],
  email: Option[String]
)

object OIDCIdentity {

  /** The required OIDC scopes for populating an instance of [[OIDCIdentity]]. */
  final val requiredScopes = Set("openid", "profile", "email")

  /** Circe [[Decoder decoder]] from a [[Json JSON]] object to [[OIDCIdentity]]. */
  implicit final val fromJson: Decoder[OIDCIdentity] =
    (c: HCursor) => {
      // Try to get the user's name multiple ways / using multiple fields
      val getUsername =
        c.get[String]("name")
          .leftFlatMap(_ => c.get[String]("preferred_username"))
          .leftFlatMap(_ => c.get[String]("nickname"))

      for {
        id            <- c.get[String]("sub").map(literalUUIDOrFromName)
        name          <- getUsername
        preferredName <- c.get[Option[String]]("preferred_username")
        familyName    <- c.get[Option[String]]("family_name")
        givenName     <- c.get[Option[String]]("given_name")
        middleName    <- c.get[Option[String]]("middle_name")
        email         <- c.get[Option[String]]("email")
      } yield OIDCIdentity(id, name, preferredName, familyName, givenName, middleName, email)
    }

  /** Circe [[Encoder encoder]] from [[OIDCIdentity]] to a [[Json JSON]] object. */
  implicit final val toJson: Encoder[OIDCIdentity] =
    (ou: OIDCIdentity) =>
      Json.obj(
        "sub"            -> ou.id.asJson,
        "name"           -> ou.name.asJson,
        "preferred_name" -> ou.preferredName.asJson,
        "family_name"    -> ou.familyName.asJson,
        "given_name"     -> ou.givenName.asJson,
        "middle_name"    -> ou.middleName.asJson,
        "email"          -> ou.email.asJson
      )

  // Utility for either parsing or populating the Identity's unique identifier.
  @inline final private def literalUUIDOrFromName(str: String) =
    try UUID.fromString(str)
    catch { case scala.util.control.NonFatal(_) => UUID.nameUUIDFromBytes(str.getBytes("UTF-8")) }

}
