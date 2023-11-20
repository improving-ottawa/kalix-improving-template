package com.improving.extensions.email

import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import com.comcast.ip4s.Host
import com.minosiants.pencil
import com.minosiants.pencil.data
import com.minosiants.pencil._
import fs2.io.net.Network
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.Instant
import java.util.UUID

final class SmtpEmailSystem private (sockAddr: SocketAddress[Host], creds: Option[data.Credentials], insecure: Boolean)
    extends EmailSystem {
  import pencil.protocol._

  private val logger = Slf4jLogger.getLoggerFromClass[IO](classOf[SmtpEmailSystem])

  val checkCanSendEmails: IO[Boolean] = {
    val netResources =
      for {
        tlsCtx        <- if (insecure) Network[IO].tlsContext.insecureResource else Network[IO].tlsContext.systemResource
        netSocket     <- Network[IO].client(sockAddr)
        smtpSocket    <- Resource.liftK(IO.pure(SmtpSocket.fromSocket(netSocket, logger)))
        smtpTlsSocket <- tlsCtx.client(netSocket).map(tlsSocket => SmtpSocket.fromSocket(tlsSocket, logger))
      } yield (smtpSocket, smtpTlsSocket)

    def supportLogin(rep: Replies): Boolean =
      rep.replies.exists(reply => reply.text.contains("AUTH") && reply.text.contains("LOGIN"))

    def supportTLS(rep: Replies): Boolean =
      rep.replies.exists(r => r.text.contains("STARTTLS"))

    def login(rep: Replies): Smtp[IO, Unit] =
      creds
        .filter(scala.Function.const(supportLogin(rep)))
        .fold(Smtp.unit[IO])(Smtp.login[IO])

    def loginTls(smtpTlsSocket: SmtpSocket[IO]): Smtp[IO, Unit] = {
      val loginFlow =
        for {
          rep <- Smtp.ehlo[IO]()
          _   <- login(rep)
        } yield ()

      Smtp.startTls[IO]().flatMap { _ =>
        Smtp.local((req: Request[IO]) =>
          Request(
            req.email,
            smtpTlsSocket,
            data.Host.local(),
            Instant.now(),
            () => UUID.randomUUID().toString
          )
        )(loginFlow)
      }
    }

    def canLogin(rep: Replies, smtpTlsSocket: SmtpSocket[IO]) = {
      val doLoginTls       = Smtp.liftF(logger.info("Logging in via TLS...")) *> loginTls(smtpTlsSocket)
      val doLoginPlainText = Smtp.liftF(logger.info("Logging in insecurely...")) *> login(rep)

      (if (supportTLS(rep)) doLoginTls else doLoginPlainText)
        .map(_ => true)
        .handleErrorWith { error =>
          Smtp
            .liftF(logger.error(error)(s"Login to SMTP server failed @ $sockAddr due to an error:"))
            .map(_ => false)
        }
    }

    netResources.use { case (smtpSocket, smtpTlsSocket) =>
      val smtpRequest =
        for {
          _   <- Smtp.init[IO]()
          rep <- Smtp.ehlo[IO]()
          res <- canLogin(rep, smtpTlsSocket)
        } yield res

      val dummyEmail = data.Email.TextEmail(
        data.From(data.Mailbox("", "")),
        data.To(data.Mailbox("", "")),
        None,
        None,
        None,
        None
      )

      smtpRequest.run(
        Request[IO](
          email = dummyEmail,
          socket = smtpSocket,
          data.Host.local(),
          Instant.now(),
          () => UUID.randomUUID().toString
        )
      )
    }
  }

  def sendEmail(email: Email): IO[Seq[SendResult]] = {
    for {
      pEmail  <- IO(emailToPencilEmail(email))
      tlsCtx  <- if (insecure) Network[IO].tlsContext.insecure else Network[IO].tlsContext.system
      client  <- IO.pure(Client[IO](sockAddr, creds)(tlsCtx, logger))
      replies <- client.send(pEmail)
    } yield {
      replies.replies.zip(email.recipients.toList).map { case (reply, reAddr) =>
        pencilReplyToSendResult(reAddr, reply)
      }
    }
  }

  private def emailToPencilEmail(email: Email): data.Email =
    email.body match {
      case EmailBody.Html(content) =>
        data.Email.mime(
          from = data.From(data.Mailbox.unsafeFromString(email.from.address)),
          to = data.To(email.recipients.map(reAddr => data.Mailbox.unsafeFromString(reAddr.address))),
          subject = data.Subject(email.subject),
          body = data.Body.Html(content),
        )

      case EmailBody.Text(content) =>
        data.Email.text(
          from = data.From(data.Mailbox.unsafeFromString(email.from.address)),
          to = data.To(email.recipients.map(reAddr => data.Mailbox.unsafeFromString(reAddr.address))),
          subject = data.Subject(email.subject),
          body = data.Body.Ascii(content),
        )
    }

  private def pencilReplyToSendResult(emailAddress: EmailAddress, reply: Reply): SendResult =
    SendResult(
      id = "(None)",
      emailAddress = emailAddress.address,
      status = if (reply.code.success) EmailStatus.Sent else EmailStatus.Rejected(reply.code.description)
    )

}

object SmtpEmailSystem {

  def apply(sockAddr: SocketAddress[Host]): SmtpEmailSystem = new SmtpEmailSystem(sockAddr, None, false)

  def apply(
    sockAddr: SocketAddress[Host],
    username: String,
    password: String,
    insecure: Boolean = false
  ): SmtpEmailSystem =
    new SmtpEmailSystem(
      sockAddr,
      Some(data.Credentials(data.Username(username), data.Password(password))),
      insecure
    )

}
