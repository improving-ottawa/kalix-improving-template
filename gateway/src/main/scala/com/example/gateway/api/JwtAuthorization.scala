package com.example.gateway.api

import cats.data.{NonEmptyChain, Validated}
import kalix.scalasdk.action.Action
import org.slf4j.Logger

/** Mixin which contains authorization helper functions for JWT authorization */
trait JwtAuthorization extends Action {
  import JwtAuthorization._

  protected def log: Logger

  final def requiresAuthorization[R](body: => Action.Effect[R]): Action.Effect[R] =
    authorizeOrFailWith(Authorize.requiresAuthorization)(body)(
      effects.error("You are not authorized to perform this action.", io.grpc.Status.Code.UNAUTHENTICATED)
    )

  protected def extractJwtAuthorization(): Authorization = {
    // Rewrite the Kalix JWT claims from the metadata
    val claims = actionContext.metadata.map(entry => (entry.key.replace("_kalix-jwt-claim-", ""), entry.value))

    // def parseUsage(text: String): LoginTokenUsage =
    //   LoginTokenUsage.fromName(text).getOrElse(throw new RuntimeException(s"Unknown token usage: $text"))

    // claims.foldLeft(Authorization()) { case (jwt, (key, value)) =>
    //  key match {
    //    case "usage"      => jwt.copy(usage = Some(parseUsage(value)))
    //    case _            => jwt
    //  }
    // }

    claims.foldLeft(Authorization()) { case (jwt, (key, value)) => jwt }
  }

  final private def authorizeOrFailWith[R](first: Authorizer, rest: Authorizer*)(succeedWith: => R)(
    failWith: => R
  ): R = {
    val authResult = Authorize(first, rest: _*)(extractJwtAuthorization())
    authResult match {
      case Right(_)       => succeedWith
      case Left(failures) => reportUnauthorizedAndReturn(failures)(failWith)
    }
  }

  final private def reportUnauthorizedAndReturn[R](failures: NonEmptyChain[String])(result: R): R = {
    val failuresString = failures.iterator.mkString("\t", "\n\t", "\n")
    log.warn(s"Action authorization failed due to:\n$failuresString")
    result
  }

}

object JwtAuthorization {

  final case class Authorization(
    // usage: Option[LoginTokenUsage] = None
  )

  final private case class Authorizer(
    verify: Authorization => Boolean,
    error: Authorization => String
  ) extends (Authorization => Validated[NonEmptyChain[String], Unit]) {

    def apply(auth: Authorization): Validated[NonEmptyChain[String], Unit] =
      if (verify(auth)) Validated.valid(()) else Validated.invalid(NonEmptyChain.one(error(auth)))

  }

  private object Authorize {

    def apply(first: Authorizer, rest: Authorizer*)(auth: Authorization): Either[NonEmptyChain[String], Unit] = {
      val authorizers = first +: rest

      val success: Validated[NonEmptyChain[String], Unit] = Validated.valid(())

      authorizers
        .foldLeft(success) { case (validated, authorizer) =>
          validated.combine(authorizer(auth))
        }
        .toEither
    }

    val requiresAuthorization: Authorizer =
      Authorizer(
        auth => auth.productIterator.nonEmpty,
        auth => s"Action requires authorization, but claims were invalid (got: $auth)."
      )

    // def requiresTokenUsage(usage: LoginTokenUsage): Authorizer =
    //  Authorizer(
    //    auth => auth.usage.contains(usage),
    //    auth => s"Action requires token usage `$usage` but JWT has usage: ${auth.usage}"
    //  )

  }

}
