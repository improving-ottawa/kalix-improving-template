package com.example.gateway

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.scaladsl.Sink

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object LocalCorsProxy {

  def main(args: Array[String]): Unit = {
    val argsView = args.view

    val proxyPort = args.view
      .collectFirst { case arg if arg.startsWith("--port") => arg.split('=').drop(1).lastOption }
      .flatten
      .flatMap(portString => Try(portString.toInt).toOption)
      .getOrElse(8010) // default proxy port

    val bindAddress = argsView
      .collectFirst { case arg if arg.startsWith("--bindAddress") => arg.split('=').drop(1).lastOption }
      .flatten
      .getOrElse("localhost") // default bind address

    val proxiedUri = Uri("http://localhost:9000")

    val allowCredentials = `Access-Control-Allow-Credentials`(true)

    implicit val system: ActorSystem                = ActorSystem()
    implicit val executionContext: ExecutionContext = system.dispatcher

    val requestHandler: HttpRequest => Future[HttpResponse] =
      request => {
        val originalUri = request.uri
        val proxyReqUri = proxiedUri
          .withPath(originalUri.path)
          .withRawQueryString(originalUri.rawQueryString.getOrElse(""))
          .withFragment(originalUri.fragment.getOrElse(""))

        println(s"Proxying request from '$originalUri' -> '$proxyReqUri'")

        val corsRequest = request
          .withUri(proxyReqUri)
          .removeHeader("Timeout-Access")

        val reqOrigin = request
          .header[Origin]
          .flatMap(_.origins.headOption)
          .map(_.value)
          .getOrElse("http://localhost:3000")

        val allowOriginHeader: HttpHeader = `Access-Control-Allow-Origin`(HttpOrigin(reqOrigin))

        Http().singleRequest(corsRequest).map { response =>
          val respCode = response.status
          println(s"\tGot response from proxied server: $respCode")

          if (response.headers[`Access-Control-Allow-Origin`].nonEmpty)
            response
              .removeHeader(`Access-Control-Allow-Origin`.name)
              .addHeader(allowOriginHeader)
              .addHeader(allowCredentials)
          else
            response
              .addHeader(allowOriginHeader)
              .addHeader(allowCredentials)
        }
      }

    println(s"Starting proxy at `$bindAddress` @ port: $proxyPort...")
    val serverSource = Http().newServerAt(bindAddress, proxyPort).connectionSource()

    serverSource
      .to(
        Sink.foreach { connection =>
          connection.handleWithAsyncHandler(requestHandler)
        }
      )
      .run()
  }

}
