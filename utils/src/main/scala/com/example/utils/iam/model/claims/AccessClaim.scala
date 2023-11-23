package com.example.utils.iam.model.claims

import com.example.utils.iam.model._
import com.example.utils.iam.model.rbac._

import java.util.UUID
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

case class AccessClaim(
  id: UUID,
  principal: IamPrincipal,
  groups: Set[Group],
  roles: Set[Role],
  permissions: Set[Permission]
) extends IamClaim

object AccessClaim {

  def apply(principal: IamPrincipal, groups: Set[Group], roles: Set[Role], permissions: Set[Permission]): AccessClaim =
    new AccessClaim(UUID.randomUUID(), principal, groups, roles, permissions)

  def fromIamPrivileges(priv: IamPrivileges) =
    AccessClaim(principal = priv, groups = priv.groups, roles = priv.roles, permissions = priv.permissions)

  implicit final val toJson: Encoder[AccessClaim] = new Encoder[AccessClaim] {

    final def apply(ac: AccessClaim) = Json.obj(
      "jti"         -> ac.id.asJson,
      "sub"         -> ac.principal.id.asJson,
      "sub_name"    -> ac.principal.name.asJson,
      "groups"      -> ac.groups.asJson,
      "roles"       -> ac.roles.asJson,
      "permissions" -> ac.permissions.asJson
    )

  }

  implicit final val fromJson: Decoder[AccessClaim] = new Decoder[AccessClaim] {

    final def apply(c: HCursor) =
      for {
        claimId     <- c.get[String]("jti").map(UUID.fromString)
        subId       <- c.get[String]("sub").map(UUID.fromString)
        subName     <- c.get[String]("sub_name")
        groups      <- c.get[Set[Group]]("groups")
        roles       <- c.get[Set[Role]]("roles")
        permissions <- c.get[Set[Permission]]("permissions")
      } yield AccessClaim(claimId, PrincipalIdentifier(subId, subName), groups, roles, permissions)

  }

}
