package com.example.utils.iam.model

import cats.syntax.either._

sealed trait Email

final case class EmailAddress(address: String) extends Email
case object EmailNone extends Email

object Email {

  /**
    * Unapply for [[Email email address]], used in pattern extraction.
    */
  final def unapply(arg: Email): Option[String] = arg match {
    case EmailAddress(address) => Some(address)
    case EmailNone             => None
  }

  /**
    * An empty email object.
    */
  final def none: Email = EmailNone

  /**
    * Parse the given address into an Email object.
    *
    * @param address
    *   The address to parse
    */
  def parse(address: String): Either[Throwable, Email] =
    if (address.isEmpty) Either.left(InvalidEmailFormat(s"Email address cannot be empty."))
    else if (address.contains("@")) Either.right(EmailAddress(address))
    else Either.left(InvalidEmailFormat(s"Provided email $address is invalid."))

  final case class InvalidEmailFormat(message: String) extends Exception(message)

  /**
    * Parse the string into a valid address or an EmailNone object if invalid.
    *
    * @param address
    *   The address to parse
    */
  def validOrNone(address: String): Email = parse(address).fold(_ => EmailNone, identity)

  /*
   * Avro serialization resources
   */
  // implicit final val emailValueEncoder: ValueEncoder[Email] = ValueEncoder[String].comap[Email](_.toString)
  // implicit final val emailValueDecoder: ValueDecoder[Email] = ValueDecoder[String].map[Email](Email.validOrNone)
  // implicit final val emailAutoSchema: AutoSchema[Email] = AutoSchema[String].map[Email](identity)

}
