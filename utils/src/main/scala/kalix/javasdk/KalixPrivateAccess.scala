package kalix.javasdk

import akka.actor.ActorSystem
import com.google.protobuf.Descriptors
import kalix.javasdk.impl.Service

private[kalix] object KalixPrivateAccess {
  import scala.jdk.CollectionConverters._

  private[this] val kalixClass = classOf[kalix.javasdk.Kalix]
  private type JavaHashMap[A, B] = java.util.HashMap[A, B]
  private type JavaSet[A]        = java.util.Set[A]
  private type RegServiceFn      = java.util.function.Function[ActorSystem, Service]

  def getKalixServices(javaKalix: Kalix): Seq[Service] = {
    val nullSystem: ActorSystem = null
    val servicesMap             = getPrivateField[JavaHashMap[java.lang.String, RegServiceFn]]("services")(javaKalix).asScala

    servicesMap.values.map(fn => fn(nullSystem)).toSeq
  }

  def registerKalixService(javaKalix: Kalix, service: Service): Kalix = {
    val allDescriptors = getPrivateField[JavaSet[Descriptors.FileDescriptor]]("allDescriptors")(javaKalix)
    val servicesMap    = getPrivateField[JavaHashMap[java.lang.String, RegServiceFn]]("services")(javaKalix).asScala

    // Add additional descriptors
    val descriptorsCollection = service.additionalDescriptors.toSeq.asJavaCollection
    allDescriptors.addAll(descriptorsCollection)

    // Add the service to the servicesMap
    servicesMap.addOne((service.descriptor.getFullName, _ => service))

    // Return the updated `javaKalix` Kalix object
    javaKalix
  }

  final private[this] def getPrivateField[A](name: String)(instance: kalix.javasdk.Kalix): A = {
    val jvmField = kalixClass.getDeclaredField(name)
    jvmField.setAccessible(true)

    val result = jvmField.get(instance)
    jvmField.setAccessible(false)

    result.asInstanceOf[A]
  }

}
