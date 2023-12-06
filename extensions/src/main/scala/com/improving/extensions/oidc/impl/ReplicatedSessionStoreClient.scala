package com.improving.extensions.oidc.impl

// Generated by Akka gRPC. DO NOT EDIT.

import scala.concurrent.ExecutionContext

import akka.actor.ClassicActorSystemProvider

import akka.grpc.GrpcChannel
import akka.grpc.GrpcClientCloseException
import akka.grpc.GrpcClientSettings

import akka.grpc.scaladsl.AkkaGrpcClient

import akka.grpc.internal.NettyClientUtils

import akka.grpc.AkkaGrpcGenerated

import akka.grpc.scaladsl.SingleResponseRequestBuilder
import akka.grpc.internal.ScalaUnaryRequestBuilder

// Not sealed so users can extend to write their stubs
@AkkaGrpcGenerated
trait ReplicatedSessionStoreClient
    extends ReplicatedSessionStore
    with ReplicatedSessionStoreClientPowerApi
    with AkkaGrpcClient

@AkkaGrpcGenerated
object ReplicatedSessionStoreClient {

  def apply(settings: GrpcClientSettings)(implicit sys: ClassicActorSystemProvider): ReplicatedSessionStoreClient =
    new DefaultReplicatedSessionStoreClient(GrpcChannel(settings), isChannelOwned = true)

  def apply(channel: GrpcChannel)(implicit sys: ClassicActorSystemProvider): ReplicatedSessionStoreClient =
    new DefaultReplicatedSessionStoreClient(channel, isChannelOwned = false)

  final private class DefaultReplicatedSessionStoreClient(channel: GrpcChannel, isChannelOwned: Boolean)(implicit
    sys: ClassicActorSystemProvider
  ) extends ReplicatedSessionStoreClient {
    import ReplicatedSessionStore.MethodDescriptors._

    implicit private val ex: ExecutionContext = sys.classicSystem.dispatcher

    private val settings = channel.settings
    private val options  = NettyClientUtils.callOptions(settings)

    private def putSessionRequestBuilder(channel: akka.grpc.internal.InternalChannel) =
      new ScalaUnaryRequestBuilder(putSessionDescriptor, channel, options, settings)

    private def getSessionRequestBuilder(channel: akka.grpc.internal.InternalChannel) =
      new ScalaUnaryRequestBuilder(getSessionDescriptor, channel, options, settings)

    /**
      * Lower level "lifted" version of the method, giving access to request metadata etc. prefer
      * putSession(com.improving.extensions.oidc.impl.StoreSessionRequest) if possible.
      */
    override def putSession(): SingleResponseRequestBuilder[StoreSessionRequest, com.google.protobuf.empty.Empty] =
      putSessionRequestBuilder(channel.internalChannel)

    /**
      * For access to method metadata use the parameterless version of putSession
      */
    def putSession(in: StoreSessionRequest): scala.concurrent.Future[com.google.protobuf.empty.Empty] =
      putSession().invoke(in)

    /**
      * Lower level "lifted" version of the method, giving access to request metadata etc. prefer
      * getSession(com.improving.extensions.oidc.impl.SessionKey) if possible.
      */
    override def getSession(): SingleResponseRequestBuilder[SessionKey, GetSessionResponse] =
      getSessionRequestBuilder(channel.internalChannel)

    /**
      * For access to method metadata use the parameterless version of getSession
      */
    def getSession(in: SessionKey): scala.concurrent.Future[GetSessionResponse] =
      getSession().invoke(in)

    override def close(): scala.concurrent.Future[akka.Done] =
      if (isChannelOwned) channel.close()
      else throw new GrpcClientCloseException()

    override def closed: scala.concurrent.Future[akka.Done] = channel.closed()
  }

}

@AkkaGrpcGenerated
trait ReplicatedSessionStoreClientPowerApi {

  /**
    * Lower level "lifted" version of the method, giving access to request metadata etc. prefer
    * putSession(com.improving.extensions.oidc.impl.StoreSessionRequest) if possible.
    */
  def putSession(): SingleResponseRequestBuilder[StoreSessionRequest, com.google.protobuf.empty.Empty] = ???

  /**
    * Lower level "lifted" version of the method, giving access to request metadata etc. prefer
    * getSession(com.improving.extensions.oidc.impl.SessionKey) if possible.
    */
  def getSession(): SingleResponseRequestBuilder[SessionKey, GetSessionResponse] = ???

}
