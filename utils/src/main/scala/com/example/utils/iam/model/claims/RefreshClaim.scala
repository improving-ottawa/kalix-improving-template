package com.example.utils.iam.model.claims

import java.util.UUID

import com.example.utils.iam.model.{IamClaim, IamPrincipal, PrincipalIdentifier}
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._

case class RefreshClaim(id: UUID, principal: IamPrincipal) extends IamClaim

object RefreshClaim {
  def apply(principal: IamPrincipal): RefreshClaim = RefreshClaim(UUID.randomUUID(), principal)

  implicit final val toJson: Encoder[RefreshClaim] = new Encoder[RefreshClaim] {

    final def apply(rc: RefreshClaim) = Json.obj(
      "jti"      -> rc.id.asJson,
      "sub"      -> rc.principal.id.asJson,
      "sub_name" -> rc.principal.name.asJson
    )

  }

  implicit final val fromJson: Decoder[RefreshClaim] = new Decoder[RefreshClaim] {

    override def apply(c: HCursor) =
      for {
        claimId <- c.get[String]("jti").map(UUID.fromString)
        subId   <- c.get[String]("sub").map(UUID.fromString)
        subName <- c.get[String]("sub_name")
      } yield RefreshClaim(claimId, PrincipalIdentifier(subId, subName))

  }

}
