package com.example.extensions

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.std._
import cats.syntax.all._
import com.example.common.common.domain.Contact
import com.example.extensions.email.RecipientType.To
import com.example.extensions.email._

object EmailSendTest extends IOApp {

  // This is a test only key
  private val apiKey     = "ec309c632c0d5eb4eb1ea6c4c5d80934"
  private val privateKey = "7e35fd00266feded508d6c8af2244705"

  val testContacts = List(
    Contact("Ex", "Ample", emailAddress = "exa.ample@example.com"),
  )

  def run(args: List[String]): IO[ExitCode] = {
    val system: EmailSystem = MailJetEmailSystem(apiKey, privateKey)

    val fromEmailAddress = FromEmailAddress("service@example.io", None)

    def createEmail(contact: Contact, emailBody: EmailBody): Email =
      Email(
        fromEmailAddress,
        "Example test email",
        emailBody,
        NonEmptyList.one(RecipientEmailAddress(contact.emailAddress, To, None))
      )

    def sendEmails(emails: List[Email]): IO[Unit] =
      emails
        .map { email =>
          val doSend = system.sendEmail(email) >>= { results =>
            results.map(result => Console[IO].println(s"Got: $result")).sequence.as(())
          }
          Console[IO].println(s"Sending email to: ${email.recipients.head}") *> doSend
        }
        .sequence
        .as(())

    val createTestEmails = testContacts.map { contact =>
      createEmailHtml(contact).map { emailBody =>
        createEmail(contact, emailBody)
      }
    }.sequence

    val checkSystem = system.checkCanSendEmails
      .ifM(
        Console[IO].println(s"${system.getClass.getSimpleName} can send emails."),
        IO.raiseError(new RuntimeException("Cannot send emails."))
      )

    for {
      emails <- createTestEmails
      _      <- checkSystem
      _      <- sendEmails(emails)
    } yield ExitCode.Success
  }
  
  private def createEmailHtml(contact: Contact): IO[EmailBody] = {
    val substitutions  = Map(
      "loginLink" -> "https://example.io/",
    )
    val templateEngine = TemplateEngine(substitutions).fromTemplateResource("/TokenForAdminLoginEmail.html")
    templateEngine.formatToString.map(EmailBody.html)
  }

}
