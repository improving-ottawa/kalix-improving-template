package com.improving.extensions.oidc.impl

import com.improving.extensions.oidc._
import com.improving.utils._

import cats.effect._
import com.google.protobuf.ByteString
import kalix.javasdk.impl.GrpcClients

import scala.concurrent.Future

/**
  * Not accessible. Instead use [[ReplicatedSessionStore]] companion object, which will force the correct Kalix
  * component registration(s).
  */
private[impl] object ReplicatedSessionStoreImpl {
  // Using a single global `entityId` so only one replicated-entity is created per deployment.
  final private val entityId: String = "92990870-9481-11ee-a434-1b7c64d6d3e8"

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

/** Not accessible / can't instantiate directly. Instead use [[ReplicatedSessionStore]] companion object. */
private class ReplicatedSessionStoreImpl private (serviceEndpoint: Option[String], port: Int)(implicit
  val asyncContext: AsyncContext
) extends SessionStore[Future] {
  import ReplicatedSessionStoreImpl.entityId
  import asyncContext._

  private lazy val client = {
    val endpoint = serviceEndpoint.getOrElse("localhost")
    GrpcClients(actorSystem).getGrpcClient(classOf[ReplicatedSessionStore], endpoint, port)
  }

  def putSession(session: OIDCSession): Future[OIDCSession] = {
    val request = StoreSessionRequest(
      entityId = entityId,
      key = session.key.toString,
      data = if (session.state.rawBytes.isEmpty) ByteString.empty else ByteString.copyFrom(session.state.rawBytes)
    )

    client.putSession(request).map(_ => session)
  }

  def getSession(key: Base64String): Future[Option[OIDCSession]] = {
    val request = SessionKey(entityId, key.toString)

    client.getSession(request).map { response =>
      response.session match {
        case Some(data) if !data.data.isEmpty => Some(OIDCSession(key, Base64String(data.toByteArray)))
        case _                                => None
      }
    }
  }

}
