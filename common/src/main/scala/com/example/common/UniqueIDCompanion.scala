package com.example.common

import scalapb._

import java.util.UUID

trait UniqueIDCompanion extends GeneratedMessageCompanion[UniqueID] {

  /** The [[TypeMapper]] for [[UniqueID]] <==> [[java.util.UUID]] */
  implicit final val uniqueIDMapper: TypeMapper[UniqueID, UUID] =
    new TypeMapper[UniqueID, UUID] {
      final def toCustom(base: UniqueID): UUID = UUID.fromString(base.uuid)
      final def toBase(id: UUID): UniqueID = UniqueID(id.toString)
    }

}
