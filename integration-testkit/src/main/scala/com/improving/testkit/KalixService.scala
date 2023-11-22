package com.improving.testkit

import kalix.scalasdk.Kalix

import scala.concurrent.duration._

/** A Kalix service definition, used during integration testing. */
case class KalixService(
  serviceName: String,
  kalix: Kalix,
  stopTimeout: Option[FiniteDuration] = None,
  aclEnabled: Boolean = false,
  advancedViews: Boolean = false,
  eventingSupport: EventingSupport = EventingSupport.TestBroker
)

object KalixService {

  private[testkit] implicit class InternalExtensions(private val svc: KalixService) extends AnyVal {

    def toEntry: internal.KalixServiceEntry =
      new internal.KalixServiceEntry(
        svc.serviceName,
        svc.kalix,
        svc.stopTimeout,
        svc.aclEnabled,
        svc.advancedViews,
        svc.eventingSupport
      )

  }

}
