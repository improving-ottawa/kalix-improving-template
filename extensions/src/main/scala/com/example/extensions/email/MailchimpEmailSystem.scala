package com.example.extensions.email

import cats.effect._
import cats.effect.std.Dispatcher
import io.circe._
import io.circe.syntax._
import sttp.client3._
import sttp.client3.httpclient.cats._
import sttp.client3.circe._
import sttp.model.{StatusCode, Uri}

import org.slf4j.LoggerFactory

final class MailchimpEmailSystem private(private val apiKey: String, backendResource: Resource[IO, SttpBackend[IO, _]]) extends EmailSystem {
  import MailchimpEmailSystem._

  private val unsafeLog = LoggerFactory.getLogger(classOf[MailchimpEmailSystem])

  private def safeLog(f: org.slf4j.Logger => Unit): IO[Unit] =
    IO(f(unsafeLog))

  val checkCanSendEmails: IO[Boolean] = backendResource.use { backend =>
    def logBadResponse(response: Response[Either[String, String]]): IO[Unit] = {
      response.body match {
        case Right(unexpected) => safeLog(_.error(s"Got an unexpected response from Mailchimp API (/user/ping): $unexpected"))
        case Left(error)       => safeLog(_.error(s"Failed to verify MailChimp API access due to: $error (${response.code})"))
      }
    }

    makeRequest("/users/ping", Json.obj())
      .response(asString)
      .send(backend)
      .flatMap {
        case Response(Right("\"PONG!\""), _, _, _, _, _) => IO.pure(true)
        case other: Response[_]                      => logBadResponse(other).as(false)
      }
  }

  def sendEmail(email: Email): IO[Seq[SendResult]] = backendResource.use { backend =>
    makeRequest("/messages/send", email.toMailchimpMessage)
      .response(asJson[Seq[SendResult]])
      .send(backend)
      .flatMap {
        case Response(Right(result), StatusCode.Ok, _, _, _, _) => IO.pure(result)
        case Response(Right(_), otherCode, _, _, _, _)          => IO.raiseError(new RuntimeException(s"Unexpected HTTP status code: $otherCode"))
        case Response(Left(error), _, _, _, _, _)               => IO.raiseError(error)
      }
  }

  private def makeRequest(functionName: String, body: Json) = {
    val keyJson = Json.obj("key" -> Json.fromString(apiKey))
    val bodyJson = keyJson.deepMerge(body)
    val postUri = Uri.unsafeParse(apiEndpoint(functionName))
    basicRequest
      .contentType("application/json")
      .body(bodyJson)
      .post(postUri)
  }

}

sealed abstract class MailchimpEmailFormatting { self: MailchimpEmailSystem.type =>
  private final def formatName(name: String): String = name.replace(" ", "")

  implicit val emailAddressEncoder: Encoder[EmailAddress] = {
    case FromEmailAddress(address, name) =>
      val baseJson = Json.obj("from_email" -> Json.fromString(address))
      name.fold(baseJson)(fromName =>
        baseJson.deepMerge(Json.obj("from_name" -> Json.fromString(formatName(fromName))))
      )

    case RecipientEmailAddress(address, recipientType, recipientName) =>
      val baseJson = Json.obj("email" -> Json.fromString(address), "type" -> Json.fromString(recipientType.code))
      recipientName.fold(baseJson)(name =>
        baseJson.deepMerge(Json.obj("name" -> Json.fromString(formatName(name))))
      )
  }

  implicit val emailBodyEncoder: Encoder[EmailBody] = {
    case text: EmailBody.Text =>
      Json.obj(
        "text" -> text.render,
        "auto_html" -> Json.fromBoolean(true)
      )

    case html: EmailBody.Html =>
      Json.obj(
        "html" -> html.render,
        "inline_css" -> Json.fromBoolean(true),
        "auto_text" -> Json.fromBoolean(true)
      )
  }

  implicit val emailMessageEncoder: Encoder[Email] =
    email => {
      emailAddressEncoder(email.from)
        .deepMerge(
          Json.obj(
            "subject" -> Json.fromString(email.subject),
            "to" -> Json.arr(email.recipients.map(emailAddressEncoder.apply).iterator.toSeq: _*)
          )
        )
        .deepMerge(email.body.asJson)
        .deepMerge(Json.fromFields(email.additionalAttributes))
    }

}

object MailchimpEmailSystem extends MailchimpEmailFormatting {
  private[this] final val apiEndpointBase = "https://mandrillapp.com/api/1.0"

  private val globalDispatcher = Dispatcher.parallel[IO]

  def apply(apiKey: String): MailchimpEmailSystem = {
    val backendResource = globalDispatcher.flatMap(dispatcher =>
      Resource.liftK(HttpClientCatsBackend(dispatcher))
    )

    new MailchimpEmailSystem(apiKey, backendResource)
  }

  private final def apiEndpoint(functionName: String): String = {
    val suffix = if (functionName.startsWith("/")) functionName else s"/$functionName"
    apiEndpointBase + suffix
  }

  protected implicit final class ToMessageExtension(private val email: Email) extends AnyVal {
    def toMailchimpMessage: Json = Json.obj(
      "message" -> email.asJson
    )
  }

}
