package com.improving.extensions.oidc.impl

import com.google.protobuf.Descriptors
import com.google.protobuf.empty.EmptyProto
import kalix.scalasdk.impl.replicatedentity.ReplicatedEntityRouter
import kalix.scalasdk.replicatedentity.{
  ReplicatedEntityContext,
  ReplicatedEntityOptions,
  ReplicatedEntityProvider,
  ReplicatedRegisterMap
}

// This code is managed by Kalix tooling.
// It will be re-generated to reflect any changes to your protobuf definitions.
// DO NOT EDIT

/**
  * A replicated entity provider that defines how to register and create the entity for the Protobuf service
  * `ReplicatedSessionStore`.
  *
  * Should be used with the `register` method in [[kalix.scalasdk.Kalix]].
  */
private[impl] object ReplicatedSessionStoreEntityProvider {

  def apply(
    entityFactory: ReplicatedEntityContext => ReplicatedSessionStoreEntity
  ): ReplicatedSessionStoreEntityProvider =
    new ReplicatedSessionStoreEntityProvider(entityFactory, ReplicatedEntityOptions.defaults)

  def apply(
    entityFactory: ReplicatedEntityContext => ReplicatedSessionStoreEntity,
    options: ReplicatedEntityOptions
  ): ReplicatedSessionStoreEntityProvider =
    new ReplicatedSessionStoreEntityProvider(entityFactory, options)

}

private[impl] class ReplicatedSessionStoreEntityProvider private (
  entityFactory: ReplicatedEntityContext => ReplicatedSessionStoreEntity,
  override val options: ReplicatedEntityOptions
) extends ReplicatedEntityProvider[ReplicatedRegisterMap[String, SessionData], ReplicatedSessionStoreEntity] {

  final type RouterType =
    ReplicatedEntityRouter[ReplicatedRegisterMap[String, SessionData], ReplicatedSessionStoreEntity]

  override def entityType: String = "replicated-session-store"

  override def newRouter(context: ReplicatedEntityContext): RouterType =
    new ReplicatedSessionStoreEntityRouter(entityFactory(context))

  override def serviceDescriptor: Descriptors.ServiceDescriptor =
    ReplicatedSessionStoreProto.javaDescriptor.findServiceByName("ReplicatedSessionStore")

  override def additionalDescriptors: Seq[Descriptors.FileDescriptor] =
    EmptyProto.javaDescriptor :: ReplicatedSessionStoreProto.javaDescriptor :: Nil

}
