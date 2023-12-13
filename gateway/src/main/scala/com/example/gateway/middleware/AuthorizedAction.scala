package com.example.gateway.middleware

import com.improving.iam.AuthToken

import akka.NotUsed
import akka.stream.scaladsl.Source
import cats.data.NonEmptySet
import io.grpc.Status.{Code => StatusCode}
import kalix.scalasdk.action.Action
import org.slf4j.Logger

object AuthorizedAction {

  sealed trait ActionResult[F[+_]] {
    def raiseError[A](error: Action.Effect[Nothing]): F[A]
  }

  object ActionResult {
    final type SourceEffect[+T] = Source[Action.Effect[T], NotUsed]

    implicit object ActionResultForActionEffect extends ActionResult[Action.Effect] {
      final def raiseError[A](error: Action.Effect[Nothing]): Action.Effect[A] = error
    }

    implicit object ActionResultForSourceEffect extends ActionResult[SourceEffect] {
      final def raiseError[A](error: Action.Effect[Nothing]): SourceEffect[A] = Source.single(error)
    }

  }

}

trait AuthorizedAction extends AuthenticatedAction {
  import AuthorizedAction._

  final protected type AuthFn[T] = AuthToken => Action.Effect[T]

  protected def log: Logger

  final private def notAuthorizedError: Action.Effect[Nothing] =
    effects.error("You are not authorized to perform this action.", StatusCode.PERMISSION_DENIED)

  final protected def authorizeAll[F[+_], T](body: => F[T])(implicit AR: ActionResult[F]): AuthToken => F[T] =
    _ => body

  final protected def withRequiredRole[F[+_], T](
    role: String
  )(body: => F[T])(implicit AR: ActionResult[F]): AuthToken => F[T] = {
    val requiredRoles = Set(role)
    authToken =>
      withRequiredRolesInternal(requiredRoles, authToken)(
        _ => body,
        () => AR.raiseError(notAuthorizedError)
      )
  }

  final protected def withRequiredRoles[F[+_], T](first: String, second: String, rest: String*)(
    body: => F[T]
  )(implicit AR: ActionResult[F]): AuthToken => F[T] = {
    val requiredRoles = (first +: second +: rest).toSet
    authToken =>
      withRequiredRolesInternal(requiredRoles, authToken)(
        _ => body,
        () => AR.raiseError(notAuthorizedError)
      )
  }

  final protected def withRequiredRoles[F[+_], T](
    roles: NonEmptySet[String]
  )(body: => F[T])(implicit AR: ActionResult[F]): AuthToken => F[T] = {
    val requiredRoles = roles.toSortedSet
    authToken =>
      withRequiredRolesInternal(requiredRoles, authToken)(
        _ => body,
        () => AR.raiseError(notAuthorizedError)
      )
  }

  final private[this] def withRequiredRolesInternal[Out](
    requiredRoles: Set[String],
    authToken: AuthToken
  )(onAuthorized: AuthToken => Out, onUnauthorized: () => Out): Out = {
    val missingRoles = requiredRoles.removedAll(authToken.roles)

    if (missingRoles.nonEmpty) {
      val sub = authToken.subject

      val errorMsg =
        s"Action requires authorization, but authorization token for '$sub' is missing the following roles: " +
          missingRoles.mkString(", ")

      log.warn(errorMsg)
      onUnauthorized()
    } else onAuthorized(authToken)
  }

}
