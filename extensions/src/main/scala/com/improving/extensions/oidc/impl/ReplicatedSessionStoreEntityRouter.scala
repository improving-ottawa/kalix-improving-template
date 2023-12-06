package com.improving.extensions.oidc.impl

import kalix.javasdk.impl.replicatedentity.ReplicatedEntityRouter.CommandHandlerNotFound
import kalix.scalasdk.impl.replicatedentity.ReplicatedEntityRouter
import kalix.scalasdk.replicatedentity.{CommandContext, ReplicatedEntity, ReplicatedRegisterMap}

// This code is managed by Kalix tooling.
// It will be re-generated to reflect any changes to your protobuf definitions.
// DO NOT EDIT

/**
  * A replicated entity handler that is the glue between the Protobuf service `ReplicatedSessionStore` and the command
  * handler methods in the `ReplicatedSessionStoreEntity` class.
  */
private[impl] class ReplicatedSessionStoreEntityRouter(entity: ReplicatedSessionStoreEntity)
    extends ReplicatedEntityRouter[ReplicatedRegisterMap[String, SessionData], ReplicatedSessionStoreEntity](entity) {

  override def handleCommand(
    commandName: String,
    data: ReplicatedRegisterMap[String, SessionData],
    command: Any,
    context: CommandContext
  ): ReplicatedEntity.Effect[_] = {

    commandName match {
      case "PutSession" =>
        entity.putSession(data, command.asInstanceOf[StoreSessionRequest])

      case "GetSession" =>
        entity.getSession(data, command.asInstanceOf[SessionKey])

      case _ => throw CommandHandlerNotFound(commandName)
    }
  }

}
