package com.improving.extensions.email

import cats.data.NonEmptyList
import cats.effect.unsafe.implicits.global

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers

class SmtpEmailSystemSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {
  val testContainer = SmtpServerContainer.create()

  override def beforeAll(): Unit = testContainer.start()

  override def afterAll(): Unit = testContainer.stop()

  "SmtpEmailSystem" should {

    "be able to login to an SMTP server" in {
      val (username, password) = (testContainer.credentials.username.value, testContainer.credentials.password.value)
      val system               = SmtpEmailSystem(testContainer.socketAddress, username, password, insecure = true)

      val didLogin = system.checkCanSendEmails.unsafeRunSync()
      didLogin mustBe true

      val badSystem   = SmtpEmailSystem(testContainer.socketAddress, username, "invalid-password", insecure = true)
      val didNotLogin = !badSystem.checkCanSendEmails.unsafeRunSync()
      didNotLogin mustBe true
    }

    "be able to send a SMTP MIME email" in {
      val (username, password) = (testContainer.credentials.username.value, testContainer.credentials.password.value)
      val system               = SmtpEmailSystem(testContainer.socketAddress, username, password, insecure = true)

      val sendResults = system.sendEmail(SmtpEmailSystemSpec.testEmail).unsafeRunSync()
      sendResults.size mustBe 1

      val firstResult = sendResults.head
      firstResult.status mustBe EmailStatus.Sent
    }

  }

}

object SmtpEmailSystemSpec {

  val testEmail = Email(
    from = FromEmailAddress("user1@mydomain.tld"),
    subject = "Test Subject",
    body = EmailBody.text("hi there3"),
    recipients = NonEmptyList.one(RecipientEmailAddress("pencil@mail.pencil.com", RecipientType.To, None))
  )

}
