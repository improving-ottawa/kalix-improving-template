package com.improving.extensions.email

import cats.data.NonEmptyList
import io.circe._

sealed trait EmailBody {
  def content: String
  final def render: Json = Json.fromString(content)
}

object EmailBody {
  final def text(content: String): EmailBody = Text(content)
  final def html(content: String): EmailBody = Html(content)

  final case class Text(content: String) extends EmailBody
  final case class Html(content: String) extends EmailBody
}

sealed trait EmailAddress {
  def address: String
  def name: Option[String]
}

final case class FromEmailAddress(address: String, name: Option[String] = None) extends EmailAddress

final case class RecipientEmailAddress(address: String, recipientType: RecipientType, name: Option[String] = None)
    extends EmailAddress

sealed abstract class RecipientType(val code: String)

object RecipientType {
  case object To  extends RecipientType("to")
  case object CC  extends RecipientType("cc")
  case object BCC extends RecipientType("bcc")
}

case class Email(
  from: FromEmailAddress,
  subject: String,
  body: EmailBody,
  recipients: NonEmptyList[RecipientEmailAddress],
  additionalAttributes: Map[String, Json] = Map.empty
)
