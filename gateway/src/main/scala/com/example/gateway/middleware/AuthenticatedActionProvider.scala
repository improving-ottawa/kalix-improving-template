package com.example.gateway.middleware

import com.google.protobuf.Descriptors
import kalix.scalasdk.action._
import kalix.scalasdk.impl.action.ActionRouter

final class AuthenticatedActionProvider[A <: Action with AuthenticatedAction] private (inner: ActionProvider[A])
    extends ActionProvider[A] {

  val options: ActionOptions =
    ActionOptions.defaults.withForwardHeaders(Set("Cookie", "X-CSRF-Token"))

  def serviceDescriptor: Descriptors.ServiceDescriptor = inner.serviceDescriptor

  def newRouter(context: ActionCreationContext): ActionRouter[A] = inner.newRouter(context)

  def additionalDescriptors: Seq[Descriptors.FileDescriptor] = inner.additionalDescriptors
}

object AuthenticatedActionProvider {

  def apply[A <: Action with AuthenticatedAction](provider: ActionProvider[A]): AuthenticatedActionProvider[A] =
    new AuthenticatedActionProvider[A](provider)

}
