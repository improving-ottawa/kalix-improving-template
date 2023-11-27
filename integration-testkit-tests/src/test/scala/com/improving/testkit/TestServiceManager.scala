package com.improving.testkit

class TestServiceManager extends internal.KalixServiceManager {
  def addForTest(kalixService: KalixService): Unit = registerService(kalixService.toEntry)

  def startAll(): Unit = startAllInstances()

  def stopAll(): Unit = stopAllServices()

  def runningServices: Seq[internal.KalixServiceEntry] = runningInstances.toSeq
}
