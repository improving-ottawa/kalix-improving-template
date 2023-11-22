package com.improving.testkit.internal

import com.improving.testkit.{EventingSupport, KalixService}
import kalix.scalasdk.Kalix

import scala.concurrent.duration.FiniteDuration

/** Internal / implementation use only! Use [[KalixService]] to define your Kalix service. */
final private[testkit] class KalixServiceEntry(
  serviceName: String,
  kalix: Kalix,
  stopTimeout: Option[FiniteDuration],
  aclEnabled: Boolean,
  advancedViews: Boolean,
  eventingSupport: EventingSupport
) extends KalixService(serviceName, kalix, stopTimeout, aclEnabled, advancedViews, eventingSupport) {
  private[this] var _jvmServicePort: Int = 0
  private[this] var _kalixProxyPort: Int = 0

  def jvmServicePort: Int = _jvmServicePort
  def kalixProxyPort: Int = _kalixProxyPort

  def jvmServicePort_=(port: Int): Unit = _jvmServicePort = port
  def kalixProxyPort_=(port: Int): Unit = _kalixProxyPort = port
}
