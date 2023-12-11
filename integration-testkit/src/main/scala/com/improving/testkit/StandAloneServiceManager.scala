package com.improving.testkit

import org.slf4j.LoggerFactory

final class StandAloneServiceManager private extends internal.KalixServiceManager {

  override protected lazy val serviceManagerLog = LoggerFactory.getLogger("KalixServiceManager")
  override protected lazy val proxyManagerLog   = LoggerFactory.getLogger("KalixProxyManager")

  def registerKalixService(service: KalixService): Unit = registerService(service.toEntry)

  def startServices(): Unit = startAllInstances()

  def stopServices(): Unit = stopAllServices()

}

object StandAloneServiceManager {

  def apply(): StandAloneServiceManager = new StandAloneServiceManager

}
