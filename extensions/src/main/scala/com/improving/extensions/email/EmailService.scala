package com.improving.extensions.email

import com.example.common.common.domain.Contact

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.unsafe.IORuntime
import org.slf4j.LoggerFactory

import scala.concurrent._

//TODO: Search and replace Example with name of app
trait EmailService {
  def sendAdminLoginEmail(recipient: Contact, loginToken: String): Future[Unit]
}

object EmailService {

  final def apply(emailSystem: EmailSystem, logAuthTokens: Boolean = false)(implicit
    executionContext: ExecutionContext
  ): EmailService = EmailServiceImpl(emailSystem, logAuthTokens)

}

final private class EmailServiceImpl private (system: EmailSystem, logAuthTokens: Boolean)(implicit
  executionContext: ExecutionContext
) extends EmailService {
  import EmailServiceImpl._

  implicit private val ioRuntime: IORuntime = IORuntime.builder().setCompute(executionContext, () => ()).build()
  private val logger                        = LoggerFactory.getLogger("com.example.utils.EmailService")

  def sendAdminLoginEmail(recipient: Contact, loginToken: String): Future[Unit] = {
    val loginLink         = adminLoginUriBase + loginToken
    val substitutionsMap  = Map(
      "contact"   -> recipient,
      "loginLink" -> loginLink
    )
    val templateEngine    = TemplateEngine(substitutionsMap).fromTemplateResource(adminLoginEmailTemplate)
    val formattedTemplate = templateEngine.formatToString
    val recipientName     = Some(s"${recipient.firstName} ${recipient.lastName}")
    val emailBody         = formattedTemplate.map { htmlContent =>
      val emailBody = EmailBody.Html(htmlContent)
      Email(
        sendFromLoginEmail,
        "Login to Example",
        emailBody,
        NonEmptyList.one(RecipientEmailAddress(recipient.emailAddress, RecipientType.To, recipientName))
      )
    }

    val sendIO = emailBody.flatMap(sendSingleEmailIO(_, "admin login"))

    val logTokenIO = IO(logAuthTokens)
      .ifM(IO(logger.info(s"Login token for '${recipient.emailAddress}' is '$loginToken'")), IO.unit)

    (sendIO <* logTokenIO).unsafeToFuture()
  }

  // Implementation

  private def sendSingleEmailIO(email: Email, emailType: String): IO[Unit] =
    system.sendEmail(email).flatMap {
      case Seq(single) =>
        if (single.status.isError) {
          IO(
            logger.error(
              s"Failed to send $emailType email to ${email.recipients.head.address} due to error: ${single.status}"
            )
          )
            .productR(IO.raiseError(new RuntimeException(s"Failed to send login email.")))
        } else {
          IO(logger.info(s"Sent $emailType email to ${email.recipients.head.address} successfully."))
        }

      case _ =>
        IO(logger.error(s"Failed to receive email send status from sent email!"))
          .productR(IO.raiseError(new RuntimeException(s"Failed to send $emailType email.")))
    }

}

private object EmailServiceImpl {

  final private val sendFromLoginEmail: FromEmailAddress =
    FromEmailAddress("service@example.io", Some("Example"))

  final private val adminLoginUriBase = "https://example.io/login-token/"

  final private val adminLoginEmailTemplate = "/TokenForAdminLoginEmail.html"

  def apply(emailSystem: EmailSystem, logAuthTokens: Boolean)(implicit
    executionContext: ExecutionContext
  ): EmailServiceImpl =
    new EmailServiceImpl(emailSystem, logAuthTokens)

}
