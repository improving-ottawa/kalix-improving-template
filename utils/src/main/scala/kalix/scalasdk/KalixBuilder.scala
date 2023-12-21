package kalix.scalasdk

import kalix.replicatedentity.ReplicatedData
import kalix.scalasdk.action.{Action, ActionProvider}
import kalix.scalasdk.eventsourcedentity.{EventSourcedEntity, EventSourcedEntityProvider}
import kalix.scalasdk.replicatedentity.{ReplicatedEntity, ReplicatedEntityProvider}
import kalix.scalasdk.valueentity.{ValueEntity, ValueEntityProvider}
import kalix.scalasdk.view.ViewProvider

sealed trait KalixBuilder {
  /**
   * Register a replicated entity using a [[ReplicatedEntityProvider]]. The concrete `ReplicatedEntityProvider` is
   * generated for the specific entities defined in Protobuf, for example `CustomerEntityProvider`.
   *
   * [[kalix.scalasdk.replicatedentity.ReplicatedEntityOptions]] can be defined by in the `ReplicatedEntityProvider `.
   *
   * @return
   *   This stateful service builder.
   */
  def register[D <: ReplicatedData, E <: ReplicatedEntity[D]](provider: ReplicatedEntityProvider[D, E]): KalixBuilder

  /**
   * Register a value based entity using a [[ValueEntityProvider]]. The concrete ` ValueEntityProvider` is generated for
   * the specific entities defined in Protobuf, for example `CustomerEntityProvider`.
   *
   * [[kalix.scalasdk.valueentity.ValueEntityOptions]] can be defined by in the `ValueEntityProvider`.
   *
   * @return
   *   This stateful service builder.
   */
  def register[S, E <: ValueEntity[S]](provider: ValueEntityProvider[S, E]): KalixBuilder

  /**
   * Register a event sourced entity using a [[EventSourcedEntityProvider]]. The concrete `EventSourcedEntityProvider`
   * is generated for the specific entities defined in Protobuf, for example `CustomerEntityProvider`.
   *
   * [[kalix.scalasdk.eventsourcedentity.EventSourcedEntityOptions]] can be defined by in the
   * `EventSourcedEntityProvider`.
   *
   * @return
   *   This stateful service builder.
   */
  def register[S, E <: EventSourcedEntity[S]](provider: EventSourcedEntityProvider[S, E]): KalixBuilder

  /**
   * Register a view using a [[ViewProvider]]. The concrete ` ViewProvider` is generated for the specific views defined
   * in Protobuf, for example ` CustomerViewProvider`.
   *
   * @return
   *   This stateful service builder.
   */
  def register(provider: ViewProvider): KalixBuilder

  /**
   * Register an action using an [[ActionProvider]]. The concrete ` ActionProvider` is generated for the specific
   * entities defined in Protobuf, for example `CustomerActionProvider`.
   *
   * @return
   *   This stateful service builder.
   */
  def register[A <: Action](provider: ActionProvider[A]): KalixBuilder

  /** Merge all registrations from this builder with another [[KalixBuilder]]. */
  def mergeWith(otherBuilder: KalixBuilder): KalixBuilder

  /** Creates a [[Kalix]] instance from this builder. */
  def build: Kalix
}

private final case class KalixBuilderImpl(registrations: Map[Class[_], Kalix => Kalix]) extends KalixBuilder {
  private def doRegistration(clazz: Class[_], fn: Kalix => Kalix): KalixBuilder = {
    if (registrations.contains(clazz)) throw new RuntimeException(s"Duplicate registration of type: ${clazz.getName}")
    copy(registrations = this.registrations + (clazz -> fn))
  }

  override def register[D <: ReplicatedData, E <: ReplicatedEntity[D]](provider: ReplicatedEntityProvider[D, E]): KalixBuilder =
    doRegistration(provider.getClass, _.register(provider))

  override def register[S, E <: ValueEntity[S]](provider: ValueEntityProvider[S, E]): KalixBuilder =
    doRegistration(provider.getClass, _.register(provider))

  override def register[S, E <: EventSourcedEntity[S]](provider: EventSourcedEntityProvider[S, E]): KalixBuilder =
    doRegistration(provider.getClass, _.register(provider))

  override def register(provider: ViewProvider): KalixBuilder =
    doRegistration(provider.getClass, _.register(provider))

  override def register[A <: Action](provider: ActionProvider[A]): KalixBuilder =
    doRegistration(provider.getClass, _.register(provider))

  override def mergeWith(otherBuilder: KalixBuilder): KalixBuilder =
    otherBuilder match {
      case KalixBuilderImpl(otherRegistrations) => this.copy(registrations = this.registrations ++ otherRegistrations)
      case _                                    => this
    }

  override def build: Kalix = registrations.values.foldLeft(Kalix())((kalix, fn) => fn(kalix))

}
