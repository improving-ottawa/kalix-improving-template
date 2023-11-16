package com.example.extensions.email

import cats.effect._
import cats.syntax.all._

import org.slf4j.LoggerFactory

import java.util.UUID

object ConsoleEmailSystem extends EmailSystem {
  private val logger = LoggerFactory.getLogger("com.ott.utils.email.ConsoleEmailSystem")

  val checkCanSendEmails: IO[Boolean] = IO.pure(true)

  def sendEmail(email: Email): IO[Seq[SendResult]] = IO
  /** EndMarker */
  {
    val consoleWriteBody =
      s"""(FAKE) Email send from: ${email.from}
         |(FAKE) Email subject: ${email.subject}, to: ${email.recipients.map(_.toString).mkString_(", ")}
         |(FAKE) Email body:\n${email.body.content}""".stripMargin

    logger.info(consoleWriteBody)

    email.recipients
      .map(recipient =>
        SendResult(
          id = UUID.randomUUID.toString,
          emailAddress = recipient.address,
          status = EmailStatus.Sent
        )
      )
      .iterator
      .toSeq
  }
}
