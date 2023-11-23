package com.example.utils.iam.model.claims

import java.util.UUID

import com.example.utils.iam.model._
import com.example.utils.iam.model.{Email, EmailNone}
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._

case class IdentityClaim(id: UUID,
                         principal: IamPrincipal,
                         customerId: UUID,
                         familyName: Option[String],
                         givenName: Option[String],
                         middleName: Option[String],
                         email: Email) extends IamClaim

object IdentityClaim {
  def apply(principal: IamPrincipal,
            customerId: UUID,
            familyName: Option[String],
            givenName: Option[String],
            middleName: Option[String],
            email: Email): IdentityClaim = IdentityClaim(UUID.randomUUID(), principal, customerId, familyName, givenName, middleName, email)

  def fromIamIdentity(identity: IamIdentity) =
    IdentityClaim(
      principal   = identity,
      customerId  = identity.customerId,
      familyName  = identity.familyName,
      givenName   = identity.givenName,
      middleName  = identity.middleName,
      email       = identity.email)

  final implicit val toJson: Encoder[IdentityClaim] = new Encoder[IdentityClaim] {
    final def apply(ic: IdentityClaim) = {
      val emailField = ic.email match {
        case EmailAddress(address) => Seq("email" -> address.asJson)
        case EmailNone             => Seq()
      }

      val icFields = Seq(
        "jti"         -> ic.id.asJson,
        "sub"         -> ic.principal.id.asJson,
        "sub_name"    -> ic.principal.name.asJson,
        "customer_id" -> ic.customerId.asJson,
        "family_name" -> ic.familyName.asJson,
        "given_name"  -> ic.givenName.asJson,
        "middle_name" -> ic.middleName.asJson,
      ) ++ emailField

      Json.obj(icFields: _*)
    }
  }

  final implicit val fromJson: Decoder[IdentityClaim] = new Decoder[IdentityClaim] {
    override def apply(c: HCursor) =
      for {
        claimId     <- c.get[String]("jti").map(UUID.fromString)
        subId       <- c.get[String]("sub").map(UUID.fromString)
        subName     <- c.get[String]("sub_name")
        customerId  <- c.get[String]("customer_id").map(UUID.fromString)
        familyName  <- c.get[Option[String]]("family_name")
        givenName   <- c.get[Option[String]]("given_name")
        middleName  <- c.get[Option[String]]("middle_name")
        email       <- c.get[Option[String]]("email").map(_.fold(Email.none)(EmailAddress))
      } yield claims.IdentityClaim(claimId, PrincipalIdentifier(subId, subName), customerId, familyName, givenName, middleName, email)
  }
}