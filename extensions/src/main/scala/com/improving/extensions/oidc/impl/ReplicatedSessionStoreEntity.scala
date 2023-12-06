package com.improving.extensions.oidc.impl

import com.google.protobuf.empty.Empty
import kalix.scalasdk.replicatedentity.ReplicatedEntity
import kalix.scalasdk.replicatedentity.ReplicatedEntityContext
import kalix.scalasdk.replicatedentity.ReplicatedRegisterMap

// This class was initially generated based on the .proto definition by Kalix tooling.
// DO NOT EDIT.

final private[impl] class ReplicatedSessionStoreEntity(context: ReplicatedEntityContext)
    extends AbstractReplicatedSessionStoreEntity {

  def putSession(
    currentData: ReplicatedRegisterMap[String, SessionData],
    request: StoreSessionRequest
  ): ReplicatedEntity.Effect[Empty] = {
    val key   = request.key
    val value = SessionData(request.data)

    effects
      .update(currentData.setValue(key, value))
      .thenReply(Empty.defaultInstance)
  }

  def getSession(
    currentData: ReplicatedRegisterMap[String, SessionData],
    sessionKey: SessionKey
  ): ReplicatedEntity.Effect[GetSessionResponse] = {
    val key      = sessionKey.key
    val response = currentData.get(key) match {
      case some @ Some(data) => if (data.data.isEmpty) GetSessionResponse(None) else GetSessionResponse(some)
      case None              => GetSessionResponse(None)
    }

    effects.delete
      .thenReply(response)
  }

}
