package com.improving.testkit.internal

import org.slf4j.LoggerFactory
import org.testcontainers.Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

private[testkit] class KalixProxy private (
  imageName: String,
  serviceName: String,
  userFunctionPort: Int,
  proxyPort: Int,
  eventingPort: Int
) extends GenericContainer[KalixProxy](DockerImageName.parse(imageName)) {

  withEnv("USER_FUNCTION_HOST", "host.testcontainers.internal")
  withEnv("USER_FUNCTION_PORT", String.valueOf(userFunctionPort))
  withEnv("HTTP_PORT", proxyPort.toString)
  withEnv("EVENTING_SUPPORT", "google-pubsub-emulator")
  withEnv("PUBSUB_EMULATOR_HOST", "host.testcontainers.internal")
  withEnv("PUBSUB_EMULATOR_PORT", eventingPort.toString)

  addFixedExposedPort(proxyPort, proxyPort)

  waitingFor(Wait.forLogMessage(".*gRPC proxy started.*", 1))

  override def start(): Unit = {
    Testcontainers.exposeHostPorts(userFunctionPort)
    Testcontainers.exposeHostPorts(eventingPort)
    super.start()
    val logConsumer = new Slf4jLogConsumer(LoggerFactory.getLogger(s"$serviceName-proxy-log"))
    followOutput(logConsumer)
  }

}

object KalixProxy {
  final val proxyImageBaseName       = "gcr.io/kalix-public/kalix-proxy"
  final val defaultProxyImageVersion = "1.1.24"

  @volatile private[this] var proxyImageName = s"$proxyImageBaseName:$defaultProxyImageVersion"

  def setProxyImageVersion(versionOnly: String): Unit = {
    proxyImageName = s"$proxyImageBaseName:$versionOnly"
  }

  final private[testkit] def apply(
    serviceName: String,
    userFunctionPort: Int,
    proxyPort: Int,
    eventingPort: Int
  ): KalixProxy = new KalixProxy(proxyImageName, serviceName, userFunctionPort, proxyPort, eventingPort)

}
