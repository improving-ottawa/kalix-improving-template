package com.example.utils.iam.model

import java.util.UUID

package rbac {

  /**
    * A user group.
    *
    * @param id
    *   The unique ID of this group.
    * @param name
    *   The name of this group.
    */
  case class Group(id: UUID, name: String) extends IamEntity

  /**
    * A RBAC role.
    *
    * @param id
    *   The unique ID of this role.
    * @param name
    *   The name of this role.
    */
  case class Role(id: UUID, name: String) extends IamEntity

  /**
    * A RBAC permission.
    *
    * @param id
    *   The unique ID of this permission.
    * @param name
    *   The name of this permission.
    */
  case class Permission(id: UUID, name: String) extends IamEntity

  /**
    * A RBAC subject assignment, which defines the roles a subject (principal) is a member of.
    *
    * @param principal
    *   The principal
    * @param roles
    *   The roles that the principal is a member of.
    */
  case class PrincipalAssignment(principal: IamPrincipal, roles: Set[Role])

  /**
    * A RBAC permission assignment, which defines the permissions assigned to a role.
    *
    * @param role
    *   The role
    * @param permissions
    *   The permissions granted to that role.
    */
  case class PermissionAssignment(role: Role, permissions: Set[Permission])
}
