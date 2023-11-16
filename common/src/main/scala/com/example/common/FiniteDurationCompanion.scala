package com.example.common

import scalapb._

import scala.concurrent.duration.{FiniteDuration => SFiniteDuration}

trait FiniteDurationCompanion extends GeneratedMessageCompanion[FiniteDuration] {

  /** The [[TypeMapper]] for [[FiniteDuration]] <==> [[scala.concurrent.duration.FiniteDuration]] */
  implicit final val finiteDurationMapper: TypeMapper[FiniteDuration, SFiniteDuration] =
    TypeMapper[FiniteDuration, SFiniteDuration](proto => SFiniteDuration(proto.length, proto.unit))(scala =>
      FiniteDuration(scala.length, scala.unit)
    )

}
