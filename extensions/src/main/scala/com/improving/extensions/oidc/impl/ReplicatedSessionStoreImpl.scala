package com.improving.extensions.oidc.impl

import com.improving.extensions.oidc._
import com.improving.utils._

import cats.effect._
import com.google.protobuf.ByteString
import kalix.javasdk.impl.GrpcClients

import scala.concurrent.Future

private[impl] object ReplicatedSessionStoreImpl {

  def catsEffect(serviceEndpoint: Option[String], port: Int)(implicit asyncContext: AsyncContext): SessionStore[IO] =
    CatsWrapper(new ReplicatedSessionStoreImpl(serviceEndpoint, port))

  def scalaFuture(serviceEndpoint: Option[String], port: Int)(implicit
    asyncContext: AsyncContext
  ): SessionStore[Future] =
    new ReplicatedSessionStoreImpl(serviceEndpoint, port)

  final private case class CatsWrapper(impl: ReplicatedSessionStoreImpl) extends SessionStore[IO] {

    def putSession(session: OIDCSession): IO[OIDCSession] =
      IO.fromFuture(IO(impl.putSession(session)))

    /** Retrieves (and removes) a [[OIDCSession session]] from the store for a given `key` if it exists. */
    def getSession(key: Base64String): IO[Option[OIDCSession]] =
      IO.fromFuture(IO(impl.getSession(key)))

  }

}

private class ReplicatedSessionStoreImpl private (serviceEndpoint: Option[String], port: Int)(implicit
  val asyncContext: AsyncContext
) extends SessionStore[Future] {
  import asyncContext._

  private lazy val client = {
    val endpoint = serviceEndpoint.getOrElse("localhost")
    GrpcClients(actorSystem).getGrpcClient(classOf[ReplicatedSessionStore], endpoint, port)
  }

  def putSession(session: OIDCSession): Future[OIDCSession] = {
    val request = StoreSessionRequest(
      key = session.key.toString,
      data = ByteString.copyFrom(session.state.rawBytes)
    )

    client.putSession(request).map(_ => session)
  }

  def getSession(key: Base64String): Future[Option[OIDCSession]] = {
    val request = SessionKey(key.toString)

    client.getSession(request).map { response =>
      response.session match {
        case Some(data) if !data.data.isEmpty => Some(OIDCSession(key, Base64String(data.toByteArray)))
        case _                                => None
      }
    }
  }

}
