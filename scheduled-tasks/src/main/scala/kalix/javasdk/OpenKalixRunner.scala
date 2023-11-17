package kalix.javasdk

import java.lang.management.ManagementFactory

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import akka.Done
import akka.actor._
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.typesafe.config.ConfigFactory
import kalix.devtools.impl.DockerComposeUtils
import kalix.javasdk.impl.{AbstractContext, DiscoveryImpl, GrpcClients, Service}
import kalix.javasdk.impl.action.ActionService
import kalix.javasdk.impl.action.ActionsImpl
import kalix.javasdk.impl.eventsourcedentity.EventSourcedEntitiesImpl
import kalix.javasdk.impl.eventsourcedentity.EventSourcedEntityService
import kalix.javasdk.impl.replicatedentity.ReplicatedEntitiesImpl
import kalix.javasdk.impl.replicatedentity.ReplicatedEntityService
import kalix.javasdk.impl.valueentity.ValueEntitiesImpl
import kalix.javasdk.impl.valueentity.ValueEntityService
import kalix.javasdk.impl.view.ViewService
import kalix.javasdk.impl.view.ViewsImpl
import kalix.javasdk.impl.workflow.WorkflowImpl
import kalix.javasdk.impl.workflow.WorkflowService
import kalix.protocol.action.ActionsHandler
import kalix.protocol.discovery.DiscoveryHandler
import kalix.protocol.event_sourced_entity.EventSourcedEntitiesHandler
import kalix.protocol.replicated_entity.ReplicatedEntitiesHandler
import kalix.protocol.value_entity.ValueEntitiesHandler
import kalix.protocol.view.ViewsHandler
import kalix.protocol.workflow_entity.WorkflowEntitiesHandler
import org.slf4j.LoggerFactory

// Copied from `kalix.javasdk.KalixRunner` but need no bullshit from Kalix API!
final class OpenKalixRunner private(
  _system: ActorSystem,
  serviceFactories: Map[String, java.util.function.Function[ActorSystem, Service]],
  aclDescriptor: Option[FileDescriptorProto],
  sdkName: String
) {

  private val log = LoggerFactory.getLogger(getClass)

  // Hidden in `KalixRunner`, what bullshit!
  implicit val system: ActorSystem = _system
  private val dockerComposeUtils = DockerComposeUtils.fromConfig(system.settings.config)

  // Hidden in `KalixRunner`, what bullshit!
  val configuration: kalix.javasdk.KalixRunner.Configuration =
    new KalixRunner.Configuration(system.settings.config.getConfig("kalix"))

  // Not available via `KalixRunner`, what bullshit!
  val grpcClients: GrpcClients = GrpcClients(system)

  private val services: Map[String, Service] = serviceFactories.toSeq.map { case (serviceName, factory) =>
    serviceName -> factory(system)
  }.toMap

  private val rootContext: Context = new AbstractContext(system) {}

  private[this] def createRoutes(): PartialFunction[HttpRequest, Future[HttpResponse]] = {

    val serviceRoutes =
      services.groupBy(_._2.getClass).foldLeft(PartialFunction.empty[HttpRequest, Future[HttpResponse]]) {

        case (route, (serviceClass, eventSourcedServices: Map[String, EventSourcedEntityService]@unchecked))
          if serviceClass == classOf[EventSourcedEntityService] =>
          val eventSourcedImpl = new EventSourcedEntitiesImpl(system, eventSourcedServices, configuration)
          route.orElse(EventSourcedEntitiesHandler.partial(eventSourcedImpl))

        case (route, (serviceClass, services: Map[String, ReplicatedEntityService]@unchecked))
          if serviceClass == classOf[ReplicatedEntityService] =>
          val replicatedEntitiesImpl = new ReplicatedEntitiesImpl(system, services)
          route.orElse(ReplicatedEntitiesHandler.partial(replicatedEntitiesImpl))

        case (route, (serviceClass, entityServices: Map[String, ValueEntityService]@unchecked))
          if serviceClass == classOf[ValueEntityService] =>
          val valueEntityImpl = new ValueEntitiesImpl(system, entityServices, configuration)
          route.orElse(ValueEntitiesHandler.partial(valueEntityImpl))

        case (route, (serviceClass, workflowServices: Map[String, WorkflowService]@unchecked))
          if serviceClass == classOf[WorkflowService] =>
          val workflowImpl = new WorkflowImpl(system, workflowServices)
          route.orElse(WorkflowEntitiesHandler.partial(workflowImpl))

        case (route, (serviceClass, actionServices: Map[String, ActionService]@unchecked))
          if serviceClass == classOf[ActionService] =>
          val actionImpl = new ActionsImpl(system, actionServices, rootContext)
          route.orElse(ActionsHandler.partial(actionImpl))

        case (route, (serviceClass, viewServices: Map[String, ViewService]@unchecked))
          if serviceClass == classOf[ViewService] =>
          val viewsImpl = new ViewsImpl(system, viewServices, rootContext)
          route.orElse(ViewsHandler.partial(viewsImpl))

        case (_, (serviceClass, _)) =>
          sys.error(s"Unknown service type: $serviceClass")
      }

    val discovery = DiscoveryHandler.partial(new DiscoveryImpl(system, services, aclDescriptor, sdkName))

    serviceRoutes.orElse(discovery).orElse { case _ => Future.successful(HttpResponse(StatusCodes.NotFound)) }
  }

  // Blocks on ActorSystem termination in `KalixRunner`, more bullshit!
  def run(): Future[Http.ServerBinding] = {
    import system.dispatcher

    import scala.concurrent.duration._

    logJvmInfo()

    // start containers if application (only possible when running locally)
    dockerComposeUtils.foreach { dcu =>
      dcu.start()

      // shutdown the containers when stopping service
      CoordinatedShutdown(system)
        .addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "stop-docker-compose") { () =>
          // note, we don't want/need to wait for the containers to stop. We just move on.
          dcu.stop()
          Future.successful(Done)
        }
    }

    val bound = Http
      .get(system)
      .newServerAt(configuration.userFunctionInterface, configuration.userFunctionPort)
      .bind(createRoutes())
      // note that DiscoveryImpl will add a task in PhaseBeforeServiceUnbind to wait
      // for proxy termination
      .map(_.addToCoordinatedShutdown(3.seconds))

    bound.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.debug("gRPC server started {}:{}", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error(
          "Failed to bind gRPC server {}:{}, terminating system. {}",
          configuration.userFunctionInterface,
          configuration.userFunctionPort,
          ex
        )
        CoordinatedShutdown(system).run(kalix.javasdk.KalixRunner.BindFailure)
    }

    bound
  }

  def terminate: Future[Terminated] = system.terminate()

  private def logJvmInfo(): Unit = {
    val osMBean = ManagementFactory.getOperatingSystemMXBean
    val memoryMBean = ManagementFactory.getMemoryMXBean
    val heap = memoryMBean.getHeapMemoryUsage
    val jvmName = sys.props.get("java.runtime.name").orElse(sys.props.get("java.vm.name")).getOrElse("")
    val jvmVersion = sys.props.get("java.runtime.version").orElse(sys.props.get("java.vm.version")).getOrElse("")

    log.info(
      "JVM [{} {}], max heap [{} MB], processors [{}]",
      jvmName,
      jvmVersion,
      heap.getMax / 1024 / 1024,
      osMBean.getAvailableProcessors
    )
  }

}

object OpenKalixRunner {
  private[this] val kalixClass = classOf[kalix.javasdk.Kalix]
  private type JavaHashMap[A, B] = java.util.HashMap[A, B]

  def apply(instance: kalix.javasdk.Kalix): OpenKalixRunner = {

    val services = getPrivateField[JavaHashMap[java.lang.String, java.util.function.Function[ActorSystem, Service]]]("services")(instance)
    val aclDescriptors = getPrivateField[java.util.Optional[FileDescriptorProto]]("aclDescriptor")(instance)
    val sdkName = kalix.javasdk.BuildInfo.name
    val system = ActorSystem("kalix", KalixRunner.prepareConfig(ConfigFactory.load()))

    new OpenKalixRunner(
      system,
      scala.jdk.javaapi.CollectionConverters.asScala(services).toMap,
      scala.jdk.javaapi.OptionConverters.toScala(aclDescriptors),
      sdkName
    )
  }

  private def getPrivateField[A](name: String)(instance: kalix.javasdk.Kalix): A = {
    val jvmField = kalixClass.getDeclaredField(name)
    jvmField.setAccessible(true)
    val result = jvmField.get(instance)
    jvmField.setAccessible(false)

    result.asInstanceOf[A]
  }

}
