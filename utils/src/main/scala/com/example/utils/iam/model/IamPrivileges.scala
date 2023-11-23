package com.example.utils.iam.model

import com.example.utils.iam.model.claims._
import com.example.utils.iam.model.rbac._

import java.util.UUID

trait IamPrivileges extends IamPrincipal {

  def groups: Set[Group]

  def roles: Set[Role]

  def permissions: Set[Permission]
}

object IamPrivileges {

  final case class IamPrivilegesImpl(
    id: UUID,
    name: String,
    groups: Set[Group],
    roles: Set[Role],
    permissions: Set[Permission]
  ) extends IamPrivileges

  /**
    * Represents privileges for a given IAM user.
    *
    * @param id
    *   The user id
    * @param name
    *   The user name
    * @param groups
    *   Groups assigned to this user
    * @param roles
    *   Roles assigned to this user
    * @param permissions
    *   Permissions assigned to this user
    */
  def apply(id: UUID, name: String, groups: Set[Group], roles: Set[Role], permissions: Set[Permission]): IamPrivileges =
    IamPrivilegesImpl(id, name, groups, roles, permissions)

  /**
    * Extract privileges from an access claim
    *
    * @param claim
    *   The access claim
    */
  def fromAccessClaim(claim: AccessClaim): IamPrivileges =
    IamPrivilegesImpl(
      id = claim.principal.id,
      name = claim.principal.name,
      groups = claim.groups,
      roles = claim.roles,
      permissions = claim.permissions
    )

}
