package com.improving.extensions.identity.oidc

import com.improving.extensions.identity.IdentityBase
import cats.syntax.all._
import io.circe._
import io.circe.syntax._
import scodec.bits._

import java.util.UUID

/**
  * An OpenID Connect user principal.
  *
  * @note
  *   This is an amalgamation of the following OIDC scopes: `openid`, `profile`, `email`, `roles`.
  *
  * @param subject
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
  *   The user's `email` address (__WHICH MIGHT NOT BE POPULATED!__)
  */
case class OIDCIdentity(
  subject: String,
  name: String,
  preferredName: Option[String],
  familyName: Option[String],
  givenName: Option[String],
  middleName: Option[String],
  email: Option[String]
) extends IdentityBase {

  final lazy val id: UUID =
    try UUID.fromString(subject)
    catch { case _: IllegalArgumentException => UUID.nameUUIDFromBytes(subject.getBytes) }

  def toByteArray: Array[Byte] = {
    BinaryWriter.newWriter
      .write(id)
      .write(subject)
      .write(name)
      .writeOptionOf.string(preferredName)
      .writeOptionOf.string(familyName)
      .writeOptionOf.string(givenName)
      .writeOptionOf.string(middleName)
      .writeOptionOf.string(email)
      .toByteArray
  }


}

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
        subject       <- c.get[String]("sub")
        name          <- getUsername
        preferredName <- c.get[Option[String]]("preferred_username")
        familyName    <- c.get[Option[String]]("family_name")
        givenName     <- c.get[Option[String]]("given_name")
        middleName    <- c.get[Option[String]]("middle_name")
        email         <- c.get[Option[String]]("email")
      } yield OIDCIdentity(subject, name, preferredName, familyName, givenName, middleName, email)
    }

  /** Circe [[Encoder encoder]] from [[OIDCIdentity]] to a [[Json JSON]] object. */
  implicit final val toJson: Encoder[OIDCIdentity] =
    (ou: OIDCIdentity) =>
      Json.obj(
        "sub"            -> ou.subject.asJson,
        "name"           -> ou.name.asJson,
        "preferred_name" -> ou.preferredName.asJson,
        "family_name"    -> ou.familyName.asJson,
        "given_name"     -> ou.givenName.asJson,
        "middle_name"    -> ou.middleName.asJson,
        "email"          -> ou.email.asJson
      )

}
