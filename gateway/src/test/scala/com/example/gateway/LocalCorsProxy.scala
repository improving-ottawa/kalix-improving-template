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
    val proxyPort = args.view
      .collectFirst { case arg if arg.startsWith("--port") => arg.split('=').drop(1).lastOption }
      .flatten
      .flatMap(portString => Try(portString.toInt).toOption)
      .getOrElse(8010)  // default proxy port

    val proxiedUri = Uri("http://localhost:9000")

    val allowOriginHeader: HttpHeader = `Access-Control-Allow-Origin`.*

    implicit val system: ActorSystem = ActorSystem()
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

        Http().singleRequest(corsRequest).map { response =>
          if (response.headers[`Access-Control-Allow-Origin`].isEmpty)
            response
              .removeHeader(`Access-Control-Allow-Origin`.name)
              .addHeader(allowOriginHeader)
          else
            response
        }
      }

    println(s"Starting proxy at `localhost` @ port: $proxyPort...")
    val serverSource = Http().newServerAt("localhost", proxyPort).connectionSource()

    serverSource.to(
      Sink.foreach { connection =>
        connection handleWithAsyncHandler requestHandler
      }
    ).run()
  }

}
