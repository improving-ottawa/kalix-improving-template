package com.improving.extensions.email

import cats.effect._
import cats.effect.std.Dispatcher
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import sttp.client3._
import sttp.client3.httpclient.cats._
import sttp.client3.circe._
import sttp.model.{StatusCode, Uri}

import org.slf4j.LoggerFactory

final class MailJetEmailSystem private (
  apiKey: String,
  privateKey: String,
  backendResource: Resource[IO, SttpBackend[IO, _]]
) extends EmailSystem {
  import MailJetEmailSystem._

  private val unsafeLog = LoggerFactory.getLogger(classOf[MailchimpEmailSystem])

  private def safeLog(f: org.slf4j.Logger => Unit): IO[Unit] =
    IO(f(unsafeLog))

  val checkCanSendEmails: IO[Boolean] = backendResource.use { backend =>
    def logBadResponse(response: Response[Either[String, String]]): IO[Unit] = {
      response.body match {
        case Right(unexpected) =>
          safeLog(_.error(s"Got an unexpected response from MailJet API (/REST/apikey/): $unexpected"))
        case Left(error)       => safeLog(_.error(s"Failed to verify MailChimp API access due to: $error (${response.code})"))
      }
    }

    makeGetRequest(s"/REST/apikey/$apiKey")
      .response(asString)
      .send(backend)
      .flatMap {
        case Response(Right(_), StatusCode.Ok, _, _, _, _) => IO.pure(true)
        case other: Response[_]                            => logBadResponse(other).as(false)
      }
  }

  def sendEmail(email: Email): IO[Seq[SendResult]] = backendResource.use { backend =>
    def handleUnexpectedResult(reply: MailjetReply, statusCode: StatusCode) =
      IO.raiseError(new RuntimeException(s"Unexpected HTTP status code $statusCode with reply: $reply"))

    makePostRequest("/send", email.toMailJetMessage)
      .response(asJson[MailjetReply])
      .send(backend)
      .flatMap {
        case Response(Right(reply), StatusCode.Created, _, _, _, _) => IO(convertReply(reply))
        case Response(Right(reply), StatusCode.Ok, _, _, _, _)      => IO(convertReply(reply))
        case Response(Right(result), otherCode, _, _, _, _)         => handleUnexpectedResult(result, otherCode)
        case Response(Left(error), _, _, _, _, _)                   => IO.raiseError(error)
      }
  }

  private def convertReply(reply: MailjetReply): Seq[SendResult] =
    reply.Sent.map { result =>
      SendResult(result.MessageUUID, result.Email, EmailStatus.Sent)
    }

  // noinspection SameParameterValue
  private def makePostRequest(functionName: String, body: Json) = {
    val postUri = Uri.unsafeParse(apiEndpoint(functionName))
    basicRequest
      .contentType("application/json")
      .auth
      .basic(apiKey, privateKey)
      .body(body)
      .post(postUri)
  }

  // noinspection SameParameterValue
  private def makeGetRequest(functionName: String) = {
    val postUri = Uri.unsafeParse(apiEndpoint(functionName))
    basicRequest
      .contentType("application/json")
      .auth
      .basic(apiKey, privateKey)
      .get(postUri)
  }

}

sealed abstract class MailJetEmailFormatting { self: MailJetEmailSystem.type =>

  implicit val emailAddressEncoder: Encoder[FromEmailAddress] = { case FromEmailAddress(address, name) =>
    val baseJson = Json.obj("FromEmail" -> Json.fromString(address))
    name.fold(baseJson)(fromName => baseJson.deepMerge(Json.obj("FromName" -> Json.fromString(fromName))))
  }

  implicit val recipientEmailAddressEncoder: Encoder[RecipientEmailAddress] = {
    case RecipientEmailAddress(address, _, recipientName) =>
      val baseJson = Json.obj("Email" -> Json.fromString(address))
      recipientName.fold(baseJson)(name => baseJson.deepMerge(Json.obj("Name" -> Json.fromString(name))))
  }

  implicit val emailBodyEncoder: Encoder[EmailBody] = {
    case text: EmailBody.Text =>
      Json.obj("Text-part" -> text.render)

    case html: EmailBody.Html =>
      Json.obj("Html-part" -> html.render)
  }

  implicit val emailMessageEncoder: Encoder[Email] =
    email => {
      emailAddressEncoder(email.from)
        .deepMerge(
          Json.obj(
            "Subject"    -> Json.fromString(email.subject),
            "Recipients" -> Json.arr(email.recipients.map(recipientEmailAddressEncoder.apply).iterator.toSeq: _*)
          )
        )
        .deepMerge(email.body.asJson)
        .deepMerge(Json.fromFields(email.additionalAttributes))
    }

}

object MailJetEmailSystem extends MailJetEmailFormatting {
  final private[this] val apiEndpointBase = "https://api.mailjet.com/v3"

  private val globalDispatcher = Dispatcher.parallel[IO]

  def apply(apiKey: String, privateKey: String): MailJetEmailSystem = {
    val backendResource = globalDispatcher.flatMap(dispatcher => Resource.liftK(HttpClientCatsBackend(dispatcher)))

    new MailJetEmailSystem(apiKey, privateKey, backendResource)
  }

  private case class MailjetReply(Sent: Seq[MailjetResult])
  private case class MailjetResult(Email: String, MessageUUID: String)

  final private def apiEndpoint(functionName: String): String = {
    val suffix = if (functionName.startsWith("/")) functionName else s"/$functionName"
    apiEndpointBase + suffix
  }

  implicit final protected class ToMessageExtension(private val email: Email) extends AnyVal {
    def toMailJetMessage: Json = email.asJson
  }

}
