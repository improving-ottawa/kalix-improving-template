package com.improving.extensions.email

import cats.effect._
import io.circe._

sealed abstract class EmailStatus(val code: String, val isError: Boolean)

object EmailStatus {
  case object Sent                        extends EmailStatus("sent", false)
  case class Queued(reason: String)       extends EmailStatus("queued", true)
  case object Scheduled                   extends EmailStatus("scheduled", false)
  case class Rejected(reason: String)     extends EmailStatus("rejected", true)
  case object Invalid                     extends EmailStatus("invalid", true)
  case class Unrecognized(status: String) extends EmailStatus("unrecognized", true)
}

case class SendResult(id: String, emailAddress: String, status: EmailStatus)

object SendResult {

  implicit val sendResultDecoder: Decoder[SendResult] =
    cursor => {
      for {
        id           <- cursor.get[String]("_id")
        email        <- cursor.get[String]("email")
        status       <- cursor.get[String]("status")
        rejectReason <- cursor.downField("reject_reason").as[Option[String]]
        queuedReason <- cursor.downField("queued_reason").as[Option[String]]
      } yield {
        val emailStatus = status match {
          case "sent"      => EmailStatus.Sent
          case "scheduled" => EmailStatus.Scheduled
          case "queued"    => EmailStatus.Queued(queuedReason.getOrElse("unspecified"))
          case "rejected"  => EmailStatus.Rejected(rejectReason.getOrElse("unspecified"))
          case "invalid"   => EmailStatus.Invalid
          case unknown     => EmailStatus.Unrecognized(unknown)
        }

        SendResult(id, email, emailStatus)
      }
    }

}

trait EmailSystem {

  def checkCanSendEmails: IO[Boolean]

  def sendEmail(email: Email): IO[Seq[SendResult]]

}
