package kalix.scalasdk

import kalix.replicatedentity.ReplicatedData
import kalix.scalasdk.action._
import kalix.scalasdk.eventsourcedentity._
import kalix.scalasdk.replicatedentity._
import kalix.scalasdk.valueentity._
import kalix.scalasdk.view._

import shapeless._
import shapeless.ops.hlist._

import scala.annotation.implicitNotFound

final class KalixBuilder[HL <: HList] private (private[scalasdk] val registrationFuncs: List[Kalix => Kalix]) {

  def numberOfRegistrations: Int = registrationFuncs.length

  /* Registration Ops (`registerView`) */

  /**
    * Register a view using a [[ViewProvider]]. The concrete ` ViewProvider` is generated for the specific views defined
    * in Protobuf, for example ` CustomerViewProvider`.
    *
    * @return
    *   This stateful service builder.
    */
  @implicitNotFound("You cannot register duplicate View type: ${V}")
  def registerView[V <: View[_], P <: ViewProvider](
    viewFn: ViewCreationContext => V,
    factoryFn: (ViewCreationContext => V) => P
  )(
    implicit notContains: NotContainsConstraint[HL, V]
  ): KalixBuilder[V :: HL] = {
    val regFunc: Kalix => Kalix = _.register(factoryFn(viewFn))
    new KalixBuilder[V :: HL](regFunc :: registrationFuncs)
  }

  /* Registration Ops (`registerProvider`) */

  /**
    * Register a value based entity using a [[ValueEntityProvider]]. The concrete ` ValueEntityProvider` is generated
    * for the specific entities defined in Protobuf, for example `CustomerEntityProvider`.
    *
    * [[kalix.scalasdk.valueentity.ValueEntityOptions]] can be defined by in the `ValueEntityProvider`.
    *
    * @return
    *   This stateful service builder.
    */
  @implicitNotFound("You cannot register duplicate ValueEntity type: ${E}")
  def registerProvider[S, E <: ValueEntity[S]](provider: ValueEntityProvider[S, E])(
    implicit notContains: NotContainsConstraint[HL, E]
  ): KalixBuilder[E :: HL] = {
    val regFunc: Kalix => Kalix = _.register(provider)
    new KalixBuilder[E :: HL](regFunc :: registrationFuncs)
  }

  /**
    * Register a replicated entity using a [[ReplicatedEntityProvider]]. The concrete `ReplicatedEntityProvider` is
    * generated for the specific entities defined in Protobuf, for example `CustomerEntityProvider`.
    *
    * [[kalix.scalasdk.replicatedentity.ReplicatedEntityOptions]] can be defined by in the `ReplicatedEntityProvider `.
    *
    * @return
    *   This stateful service builder.
    */
  @implicitNotFound("You cannot register duplicate ReplicatedEntity type: ${E}")
  def registerProvider[D <: ReplicatedData, E <: ReplicatedEntity[D]](provider: ReplicatedEntityProvider[D, E])(
    implicit notContains: NotContainsConstraint[HL, E]
  ): KalixBuilder[E :: HL] = {
    val regFunc: Kalix => Kalix = _.register(provider)
    new KalixBuilder[E :: HL](regFunc :: registrationFuncs)
  }

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
  @implicitNotFound("You cannot register duplicate EventSourcedEntity type: ${E}")
  def registerProvider[S, E <: EventSourcedEntity[S]](provider: EventSourcedEntityProvider[S, E])(
    implicit notContains: NotContainsConstraint[HL, E]
  ): KalixBuilder[E :: HL] = {
    val regFunc: Kalix => Kalix = _.register(provider)
    new KalixBuilder[E :: HL](regFunc :: registrationFuncs)
  }

  /**
    * Register an action using an [[ActionProvider]]. The concrete ` ActionProvider` is generated for the specific
    * entities defined in Protobuf, for example `CustomerActionProvider`.
    *
    * @return
    *   This stateful service builder.
    */
  @implicitNotFound("You cannot register duplicate Action type: ${A}")
  def registerProvider[A <: Action](provider: ActionProvider[A])(
    implicit notContains: NotContainsConstraint[HL, A]
  ): KalixBuilder[A :: HL] = {
    val regFunc: Kalix => Kalix = _.register(provider)
    new KalixBuilder[A :: HL](regFunc :: registrationFuncs)
  }

  /* Registration Ops (`registerViaProviderApply`) */

  @implicitNotFound("You cannot register duplicate ReplicatedEntity type: ${E}")
  def registerViaProviderApply[D <: ReplicatedData, E <: ReplicatedEntity[D], P <: ReplicatedEntityProvider[D, E]](
    applyFn: (ReplicatedEntityContext => E) => P
  )(genFn: ReplicatedEntityContext => E)(
    implicit notContains: NotContainsConstraint[HL, E]
  ): KalixBuilder[E :: HL] = registerProvider(applyFn(genFn))

  @implicitNotFound("You cannot register duplicate ValueEntity type: ${E}")
  def registerViaApplyFn[S, E <: ValueEntity[S], P <: ValueEntityProvider[S, E]](
    applyFn: (ValueEntityContext => E) => P
  )(genFn: ValueEntityContext => E)(
    implicit notContains: NotContainsConstraint[HL, E],
    d1: DummyImplicit
  ): KalixBuilder[E :: HL] = registerProvider(applyFn(genFn))

  @implicitNotFound("You cannot register duplicate EventSourcedEntity type: ${E}")
  def registerViaApplyFn[S, E <: EventSourcedEntity[S], P <: EventSourcedEntityProvider[S, E]](
    applyFn: (EventSourcedEntityContext => E) => P
  )(genFn: EventSourcedEntityContext => E)(
    implicit notContains: NotContainsConstraint[HL, E],
    d1: DummyImplicit,
    d2: DummyImplicit
  ): KalixBuilder[E :: HL] = registerProvider(applyFn(genFn))

  @implicitNotFound("You cannot register duplicate Action type: ${A}")
  def registerViaApplyFn[A <: Action, P <: ActionProvider[A]](
    applyFn: (ActionCreationContext => A) => P,
    genFn: ActionCreationContext => A
  )(
    implicit notContains: NotContainsConstraint[HL, A],
    d1: DummyImplicit,
    d2: DummyImplicit,
    d3: DummyImplicit
  ): KalixBuilder[A :: HL] = registerProvider(applyFn(genFn))

  /* Builder Ops */

  @implicitNotFound("You cannot merge KalixBuilder[${L}] with this KalixBuilder due to conflicting registered types.")
  def mergeWith[L <: HList](other: KalixBuilder[L])(
    implicit diff: Diff.Aux[HL, L, HL],
    union: Union[HL, L]
  ): KalixBuilder[union.Out] = {
    val allFuncs = registrationFuncs ++ other.registrationFuncs
    new KalixBuilder[union.Out](allFuncs)
  }

}

object KalixBuilder {

  /** Create a new [[KalixBuilder]] which can be used to create a [[Kalix]] instance. */
  final def emptyBuilder: KalixBuilder[HNil] = new KalixBuilder[HNil](List.empty)

  implicit class BuildExtension[H, T <: HList](private val builder: KalixBuilder[H :: T]) extends AnyVal {

    /** Creates a [[Kalix]] instance from this builder. */
    final def build: Kalix = builder.registrationFuncs.foldLeft(Kalix())((kalix, fn) => fn(kalix))

  }

  implicit class AutoWireExtension[L <: HList](private val builder: KalixBuilder[L]) extends AnyVal {

    final def autoWire[E](
      implicit autoWired: AutoWired[E],
      notContained: NotContainsConstraint[L, E]
    ): KalixBuilder[E :: L] = autoWired(builder)

  }

}

sealed trait AutoWired[A] {
  type Context

  def entityFactory: AutoWireTypes.ContextFactory.Aux[Context, A]
  def providerFactory: AutoWireTypes.KalixProviderFactory.Aux[Context, A]

  final def apply[HL <: HList](builder: KalixBuilder[HL])(
    implicit notContained: NotContainsConstraint[HL, A]
  ): KalixBuilder[A :: HL] = providerFactory.applyTo(builder)(entityFactory.apply)

}

object AutoWired {
  import AutoWireTypes.{ContextFactory, KalixProviderFactory}

  final type Aux[C, A] = AutoWired[A] { type Context = C }

  implicit final def resolve[E](implicit pf: KalixProviderFactory[E], ef: ContextFactory[E]): Aux[pf.In, E] =
    new AutoWired[E] {
      final type Context = pf.In

      final val entityFactory: ContextFactory.Aux[pf.In, E]         = ef.asInstanceOf[ContextFactory.Aux[pf.In, E]]
      final val providerFactory: KalixProviderFactory.Aux[pf.In, E] = pf
    }

}

object AutoWireTypes {
  import scala.language.experimental.macros
  import scala.reflect.macros.whitebox

  trait ContextFactory[Out] {
    type In

    def apply(context: In): Out
  }

  object ContextFactory {
    final type Aux[In0, Out] = ContextFactory[Out] { type In = In0 }

    type ReplicatedEntityFactory[E <: ReplicatedEntity[_]] = Aux[ReplicatedEntityContext, E]
    type ValueEntityFactory[E <: ValueEntity[_]]           = Aux[ValueEntityContext, E]
    type EventSourcedFactory[E <: EventSourcedEntity[_]]   = Aux[EventSourcedEntityContext, E]
    type ActionFactory[A <: Action]                        = Aux[ActionCreationContext, A]

    implicit def mkReplicatedEntityFactory[E <: ReplicatedEntity[_]]: ReplicatedEntityFactory[E] =
      macro mkContextFactorImpl[ReplicatedEntityContext, E]

    implicit def mkValueEntityFactory[E <: ValueEntity[_]]: ValueEntityFactory[E] =
      macro mkContextFactorImpl[ValueEntityContext, E]

    implicit def mkEventSourcedEntityFactory[E <: EventSourcedEntity[_]]: EventSourcedFactory[E] =
      macro mkContextFactorImpl[EventSourcedEntityContext, E]

    implicit def mkActionFactory[A <: Action]: ActionFactory[A] =
      macro mkContextFactorImpl[ActionCreationContext, A]

    def mkContextFactorImpl[In : c.WeakTypeTag, Target : c.WeakTypeTag](c: whitebox.Context): c.Tree = {
      import c.universe._

      val inTpe       = weakTypeOf[In]
      val targetTpe   = weakTypeOf[Target]
      val classSymbol = targetTpe.typeSymbol.asClass

      val constructor = classSymbol.primaryConstructor.asMethod
      val ctorArgs    = constructor.paramLists.head

      if (ctorArgs.head.info != inTpe) {
        c.abort(c.enclosingPosition, s"$targetTpe does not have a single argument constructor of type $inTpe")
      }

      q"""new AutoWireTypes.ContextFactory[$targetTpe] {
          final type In = $inTpe
          final def apply(context: $inTpe): $targetTpe = new ${targetTpe.typeSymbol.name.toTypeName}(context)
       }"""
    }

  }

  trait KalixProviderFactory[E] {
    type In

    type ProviderType
    def apply(factoryFn: In => E): ProviderType

    def applyTo[HL <: HList](builder: KalixBuilder[HL])(factoryFn: In => E)(
      implicit notContained: NotContainsConstraint[HL, E]
    ): KalixBuilder[E :: HL]

  }

  object KalixProviderFactory {
    final type Aux[In0, E] = KalixProviderFactory[E] { type In = In0 }

    type ReplicatedEntityFactory[E <: ReplicatedEntity[_]] = Aux[ReplicatedEntityContext, E]
    type ValueEntityFactory[E <: ValueEntity[_]]           = Aux[ValueEntityContext, E]
    type EventSourcedFactory[E <: EventSourcedEntity[_]]   = Aux[EventSourcedEntityContext, E]
    type ActionFactory[A <: Action]                        = Aux[ActionCreationContext, A]

    implicit def mkReplicatedEntityFactory[E <: ReplicatedEntity[_]]: ReplicatedEntityFactory[E] =
      macro mkKalixProviderFactoryImpl[ReplicatedEntityContext, E]

    implicit def mkValueEntityFactory[E <: ValueEntity[_]]: ValueEntityFactory[E] =
      macro mkKalixProviderFactoryImpl[ValueEntityContext, E]

    implicit def mkEventSourcedFactory[E <: EventSourcedEntity[_]]: EventSourcedFactory[E] =
      macro mkKalixProviderFactoryImpl[EventSourcedEntityContext, E]

    implicit def mkActionFactory[A <: Action]: ActionFactory[A] =
      macro mkKalixProviderFactoryImpl[ActionCreationContext, A]

    def mkKalixProviderFactoryImpl[In : c.WeakTypeTag, T : c.WeakTypeTag](c: whitebox.Context): c.Tree = {
      import c.universe._

      val inTpe      = weakTypeOf[In]
      val entityTpe  = weakTypeOf[T]
      val entityName = entityTpe.typeSymbol.fullName

      val targetProviderClassSymbol = rootMirror.staticClass(s"${entityName}Provider")
      val targetProviderTpe         = targetProviderClassSymbol.toType
      val targetProviderCompanion   = targetProviderTpe.companion

      val applyFnMethodSymbol = targetProviderCompanion.decls
        .collectFirst { case m: MethodSymbol if m.name.decodedName.toString == "apply" => m }
        .getOrElse(c.abort(c.enclosingPosition, s"No `apply` method found on: $targetProviderTpe"))

      q"""
new AutoWireTypes.KalixProviderFactory[$entityTpe] {
  import shapeless._

  final type In = $inTpe

  final type ProviderType = $targetProviderTpe
  final type EntityNotIn[L <: HList] = NotContainsConstraint[L, $entityTpe]

  final def apply(factoryFn: $inTpe => $entityTpe): $targetProviderTpe = $applyFnMethodSymbol(factoryFn)

  final def applyTo[HL <: HList](builder: KalixBuilder[HL])(factoryFn: $inTpe => $entityTpe)
                                (implicit notContained: EntityNotIn[HL]): KalixBuilder[$entityTpe :: HL] =
    builder.registerProvider(apply(factoryFn))
}"""

    }

  }

}
