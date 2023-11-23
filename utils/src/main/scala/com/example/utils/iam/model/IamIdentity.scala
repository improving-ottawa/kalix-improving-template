package com.example.utils.iam.model

import java.util.UUID

import com.example.utils.iam.model.claims.IdentityClaim

trait IamIdentity extends IamPrincipal {

  def customerId: UUID

  def familyName: Option[String]

  def givenName: Option[String]

  def middleName: Option[String]

  def email: Email
}

object IamIdentity {

  final case class IamIdentityImpl(
    id: UUID,
    name: String,
    customerId: UUID,
    familyName: Option[String],
    givenName: Option[String],
    middleName: Option[String],
    email: Email
  ) extends IamIdentity

  /**
    * The identity of a given IAM user.
    *
    * @param id
    *   The user id
    * @param name
    *   The user name
    * @param customerId
    *   The customer id
    * @param familyName
    *   The family name (optional)
    * @param givenName
    *   The given name (optional)
    * @param middleName
    *   The middle name (optional)
    * @param email
    *   Email address (optional)
    */
  def apply(
    id: UUID,
    name: String,
    customerId: UUID,
    familyName: Option[String],
    givenName: Option[String],
    middleName: Option[String],
    email: Email
  ): IamIdentity = IamIdentityImpl(id, name, customerId, familyName, givenName, middleName, email)

  /**
    * Build an identity from an identity claim.
    * @param claim
    *   The identity claim.
    */
  def fromIdentityClaim(claim: IdentityClaim): IamIdentity =
    IamIdentityImpl(
      id = claim.principal.id,
      name = claim.principal.name,
      customerId = claim.customerId,
      familyName = claim.familyName,
      givenName = claim.givenName,
      middleName = claim.middleName,
      email = claim.email
    )

  /*
   * Avro serialization implicits
   */

  // implicit final val iamIdentityValueEncoder: ValueEncoder[IamIdentity] =
  //  ValueEncoder[IamIdentityImpl].comap[IamIdentity](id =>
  //    IamIdentityImpl(
  //      id = id.id,
  //      name = id.name,
  //      customerId = id.customerId,
  //      familyName = id.familyName,
  //      givenName = id.givenName,
  //      middleName = id.middleName,
  //      email = id.email))
//
  // implicit final val iamIdentityValueDecoder: ValueDecoder[IamIdentity] =
  //  ValueDecoder[IamIdentityImpl].map[IamIdentity](identity)
//
  // implicit final val iamIdentityAutoSchema: AutoSchema[IamIdentity] =
  //  AutoSchema[IamIdentityImpl].map[IamIdentity](identity)

}
