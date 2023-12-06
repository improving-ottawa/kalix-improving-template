package com.improving.extensions.oidc.impl

import com.google.protobuf.empty.Empty
import kalix.scalasdk.replicatedentity.{ReplicatedEntity, ReplicatedRegisterMap, ReplicatedRegisterMapEntity}

// This code is managed by Kalix tooling.
// It will be re-generated to reflect any changes to your protobuf definitions.
// DO NOT EDIT

abstract class AbstractReplicatedSessionStoreEntity extends ReplicatedRegisterMapEntity[String, SessionData] {

  def putSession(
    currentData: ReplicatedRegisterMap[String, SessionData],
    storeSessionRequest: StoreSessionRequest
  ): ReplicatedEntity.Effect[Empty]

  def getSession(
    currentData: ReplicatedRegisterMap[String, SessionData],
    sessionKey: SessionKey
  ): ReplicatedEntity.Effect[GetSessionResponse]

}
