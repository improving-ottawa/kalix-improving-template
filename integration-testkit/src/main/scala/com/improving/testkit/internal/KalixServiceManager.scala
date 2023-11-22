package com.improving.testkit.internal

import com.improving.testkit._

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import kalix.javasdk.OpenKalixRunner
import kalix.javasdk.testkit.{EventingTestKit, KalixProxyContainer}
import kalix.scalasdk.WrappedKalix
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.control.NonFatal

import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicReference

/** Keeps track of registered [[KalixService Kalix services]], port mappings, etc. */
private[testkit] abstract class KalixServiceManager {
  import KalixServiceManager._

  private[this] final val serviceRegistry = new AtomicReference[Map[String, KalixServiceEntry]](Map.empty)
  private[this] final val instanceRegistry = scala.collection.mutable.Map.empty[String, KalixInstance]
  private[this] final val eventingTestKits = new AtomicReference[Map[String, EventingTestKit]](Map.empty)

  @volatile private[this] var isRunning = false
  @volatile private[this] var grpcClientsConfig = ConfigFactory.empty()

  private lazy val serviceManagerLog = LoggerFactory.getLogger("IntegrationTestKit.KalixServicesManager")
  private lazy val proxyManagerLog   = LoggerFactory.getLogger("IntegrationTestKit.KalixProxyManager   ")

  /* Public API */

  final def services: Iterable[KalixService] =
    serviceRegistry.get().values

  final def hasRegisteredService(name: String): Boolean =
    serviceRegistry.get().contains(name)

  /* Protected API */

  protected final def getClientsConfig: Config = grpcClientsConfig

  @tailrec protected final def registerService(kalixService: KalixServiceEntry): Unit = {
    val registry = serviceRegistry.get()
    val svcName = kalixService.serviceName

    // Check that we do not already have a service registered under the same name
    if (registry.contains(svcName)) {
      throw new IntegrationTestError(s"A Kalix service is already registered with name: $svcName")
    }

    val updatedMap = registry.updated(kalixService.serviceName, kalixService)

    if (!serviceRegistry.compareAndSet(registry, updatedMap)) { registerService(kalixService) }
  }

  @tailrec protected final def deregisterService(kalixService: KalixServiceEntry): Unit = {
    val registry = serviceRegistry.get()
    val svcName = kalixService.serviceName

    if (!registry.contains(svcName)) ()
    else {
      val updatedRegistry = registry.removed(svcName)

      if (!serviceRegistry.compareAndSet(registry, updatedRegistry)) { deregisterService(kalixService) }
    }
  }

  protected final def kalixProxyPortMappings: Map[String, Int] =
    serviceRegistry.get().map { case (key, entry) => (key, entry.kalixProxyPort) }

  protected final def runningInstances: Iterable[KalixServiceEntry] =
    instanceRegistry.keys.map(svcName => serviceRegistry.get()(svcName))

  protected final def startAllInstances(): Unit = {
    val services = Map.from(serviceRegistry.get())

    @inline def serviceNotRunning(name: String): Boolean = !instanceRegistry.contains(name)

    def createInstance(serviceEntry: KalixServiceEntry): KalixInstance = {
      val svcName = serviceEntry.serviceName
      val svcPort = serviceEntry.jvmServicePort
      val proxyHostPort = serviceEntry.kalixProxyPort
      val cfgString =
        s"""kalix.user-function-interface = 0.0.0.0
           |kalix.user-function-port = $svcPort
           |kalix.dev-mode.docker-compose-file = none
           |kalix.system.akka.coordinated-shutdown.exit-jvm = off""".stripMargin

      val config = grpcClientsConfig.withFallback(ConfigFactory.parseString(cfgString))
      val wrapped = WrappedKalix(serviceEntry.kalix)
      val runner = wrapped.createRunner(Some(config))

      serviceManagerLog.info(s"Starting Kalix service `$svcName` on TCP port $svcPort ...")
      Await.result(runner.run(), FiniteDuration(15, "seconds"))
      serviceManagerLog.info(s"Kalix service `$svcName` started.")

      proxyManagerLog.info(s"Starting Kalix proxy for `$svcName` on (Host) TCP port $proxyHostPort ...")
      val kalixProxy = startKalixProxyFor(serviceEntry, runner)
      proxyManagerLog.info(s"Kalix proxy for `$svcName` started.")

      KalixInstance(svcName, svcPort, runner, kalixProxy)
    }

    def assignPorts(entry: KalixServiceEntry): Unit = {
      val svcName = entry.serviceName
      serviceManagerLog.info(s"Assigning TCP ports for Kalix service: $svcName ...")

      entry.jvmServicePort = availableLocalPort()
      entry.kalixProxyPort = availableLocalPort()
    }

    try synchronized {
      // Make sure the services and proxies are not already running
      if (isRunning)
        throw new IntegrationTestError("IntegrationTestKit already running")

      // Start by assigning TCP ports for the `userFunctionPort` and `kalixProxyPort` for all services.
      // This is necessary due to the fact we need all of the port mappings before creating any Kalix
      // proxy containers.
      serviceManagerLog.info("Assigning TCP ports for Kalix services and proxies...")
      for (entry <- services.values) assignPorts(entry)

      // Create the gRPC Clients config, needed by Kalix services as well as the TestKit
      grpcClientsConfig = createClientsConfig(kalixProxyPortMappings)

      // Next go through each service, start the JVM Kalix Service, then start the Kalix proxy for that service
      for (entry <- services.values) {
        if (serviceNotRunning(entry.serviceName)) {
          val instance = createInstance(entry)
          instanceRegistry += (entry.serviceName -> instance)
        }
      }

      isRunning = true
    } catch { case NonFatal(error) =>
      stopAllServices()
      throw new IntegrationTestError("Could not create one or more Kalix service(s)", Some(error))
    }
  }

  protected final def stopAllServices(): Unit = {
    val runningInstances = instanceRegistry.values

    try synchronized {
      for (instance <- runningInstances) {
        val svcName = instance.serviceName
        val kalixProxy = instance.kalixProxy

        if (kalixProxy.isRunning) {
          proxyManagerLog.info(s"Stopping Kalix proxy for service: $svcName ...")
          try  {
            kalixProxy.stop()
            proxyManagerLog.info(s"Kalix proxy for service `$svcName` stopped.")
          } catch { case e: Throwable =>
            proxyManagerLog.error(s"Could not stop Kalix proxy container for service: $svcName", e)
          }
        }

        serviceManagerLog.info(s"Stopping Kalix service: $svcName ...")
        try {
          Await.result(instance.runner.terminate, FiniteDuration(15, "seconds"))
          serviceManagerLog.info(s"Kalix service `$svcName` stopped.")
        } catch { case NonFatal(error) =>
          serviceManagerLog.error(s"Could not stop Kalix ActorSystem for service: $svcName", error)
        }
      }

      isRunning = false
    } catch { case NonFatal(error) =>
        throw new IntegrationTestError("Could not stop one or more running Kalix service(s)", Some(error))
    }

  }

  /* Internal Implementation */

  private final def startKalixProxyFor(entry: KalixServiceEntry, runner: OpenKalixRunner): KalixProxy = {
    val userFunctionPort = entry.jvmServicePort
    val proxyPort = entry.kalixProxyPort
    val svcName = entry.serviceName
    val eventingBackendPort = getEventingBackendPort(entry, runner.system)
    val proxyContainer = KalixProxy(entry.serviceName, userFunctionPort, proxyPort, eventingBackendPort)

    proxyContainer.addEnv("SERVICE_NAME", svcName)
    proxyContainer.addEnv("ACL_ENABLED", entry.aclEnabled.toString)
    proxyContainer.addEnv("VIEW_FEATURES_ALL", entry.advancedViews.toString)

    val javaOptions = new java.util.ArrayList[String]()
    javaOptions.add("-Dlogback.configurationFile=logback-dev-mode.xml")

    if (entry.eventingSupport == EventingSupport.TestBroker) {
      javaOptions.add("-Dkalix.proxy.eventing.support=grpc-backend")
      javaOptions.add("-Dkalix.proxy.eventing.grpc-backend.host=host.testcontainers.internal")
      javaOptions.add("-Dkalix.proxy.eventing.grpc-backend.port=" + eventingBackendPort)
    } else if (entry.eventingSupport == EventingSupport.GooglePubSub) {
      javaOptions.add("-Dkalix.proxy.eventing.support=kafka")
      javaOptions.add(
        "-Dkalix.proxy.eventing.kafka.bootstrap-servers=host.testcontainers.internal:" + eventingBackendPort)
    }

    kalixProxyPortMappings.foreach { case (serviceName, hostPort) =>
      javaOptions.add("-Dkalix.dev-mode.service-port-mappings." + serviceName + "=host.docker.internal:" + hostPort)
    }

    proxyContainer.addEnv("JAVA_TOOL_OPTIONS", String.join(" ", javaOptions))
    proxyContainer.start()

    proxyContainer
  }

  private final def getEventingBackendPort(entry: KalixServiceEntry, system: ActorSystem): Int =
    entry.eventingSupport match {
      case EventingSupport.Kafka        => KalixProxyContainer.DEFAULT_KAFKA_PORT
      case EventingSupport.GooglePubSub => KalixProxyContainer.DEFAULT_GOOGLE_PUBSUB_PORT
      case _                            => startEventingTestKit(system, entry)
    }

  private final def startEventingTestKit(system: ActorSystem, entry: KalixServiceEntry): Int = {
    val port = availableLocalPort()
    val codec = entry.kalix.getMessageCodec()
    val svcName = entry.serviceName

    serviceManagerLog.info(s"Starting EventingTestKit for `$svcName` on port: " + port)
    val eventingTestKit = EventingTestKit.start(system, "0.0.0.0", port, codec)
    eventingTestKits.updateAndGet(map => map.updated(svcName, eventingTestKit))
    port
  }

  private def createClientsConfig(portMapping: Map[String, Int]) = {
    val clientServiceConfigs = portMapping.map { case (svcName, port) => createServiceConfigEntry(svcName, port) }
    val configText =
      s"""akka.grpc.client {
         |${clientServiceConfigs.mkString("\n")}
         |}""".stripMargin

    ConfigFactory.parseString(configText)
  }

}

private object KalixServiceManager {

  private case class KalixInstance(
    serviceName: String,
    servicePort: Int,
    runner: OpenKalixRunner,
    kalixProxy: KalixProxy
  )

  private final def availableLocalPort(): Int = {
    var socket: Option[ServerSocket] = None
    try {
      socket = Some(new ServerSocket(0))
      socket.get.getLocalPort
    }
    catch { case e: IOException =>
      throw new IntegrationTestError("Could not get an available local port", Some(e))
    } finally {
      // Cleanup: Always close the socket!
      socket.foreach(_.close())
    }
  }

  private final def createServiceConfigEntry(serviceName: String, port: Int) =
    s"""  $serviceName {
       |    service-discovery {
       |      service-name = "$serviceName"
       |    }
       |    host = "localhost"
       |    port = $port
       |    use-tls = false
       |    deadline = 1m
       |  }""".stripMargin

}
