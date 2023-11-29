package com.improving.utils.iam

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.circe.parser._
import kalix.scalasdk.action.{Action, ActionContext}
import pdi.jwt.JwtClaim
import org.slf4j.Logger

import scala.util.Try

/**
  * Mixin trait for adding __authorization__ to your [[Action Kalix actions]]. The Authorization requires that gRPC
  * requests contain a [[AuthToken authorization token]] for each principal, encoded as a Json Web Token (JWT).
  *
  * @see
  *   [[KalixAuthorization.requiresRolesEffect]] to require one or more specific roles for an [[Action.Effect]].
  *
  * @see
  *   [[KalixAuthorization.requiresRolesStreaming]] to require one or more specific roles for a streaming [[Source]] of
  *   [[Action.Effect effects]].
  */
trait KalixAuthorization extends Action {
  import KalixAuthorization._

  /** Must implement in your [[Action Kalix Action]]. */
  protected def log: Logger

  /* Protected API */

  /** Type alias for Kalix streaming reply type. */
  final protected type StreamingReply[R] = Source[Action.Effect[R], NotUsed]

  /** Marks a [[Action Kalix Action]] function as requiring one (or more) specific authorization `roles`. */
  final protected def requiresRolesEffect[R](role: String, others: String*)(
    body: => Action.Effect[R]
  ): Action.Effect[R] =
    requiresRolesOrFail(
      (role +: others).toSet,
      _ => body,
      errMsg => effects.error(errMsg, io.grpc.Status.Code.PERMISSION_DENIED)
    )

  /** Marks a [[Action Kalix Action]] streaming function as requiring one (or more) specific authorization `roles`. */
  final protected def requiresRolesStreaming[R](role: String, others: String*)(
    stream: => StreamingReply[R]
  ): StreamingReply[R] =
    requiresRolesOrFail(
      (role +: others).toSet,
      _ => stream,
      errMsg => Source.single(effects.error(errMsg, io.grpc.Status.Code.PERMISSION_DENIED))
    )

  /* Internal Implementation */

  final private def requiresRolesOrFail[Out](
    allRequiredRoles: Set[String],
    onAuthorized: AuthToken => Out,
    onUnauthorized: String => Out,
  ): Out = {
    val authTokenAttempt = extractAuthToken(actionContext)

    authTokenAttempt
      .map { authToken =>
        val missingRoles = allRequiredRoles.removedAll(authToken.roles)

        // If the `AuthToken` is missing any roles
        if (missingRoles.nonEmpty) {
          val sub      = authToken.subject
          val errorMsg =
            s"Action requires authorization, but authorization token for '$sub' is missing the following roles: " +
              missingRoles.mkString(", ")

          log.warn(errorMsg)
          onUnauthorized("You are not authorized to perform this action.")
        }
        // Otherwise, the principal has all required roles, so invoke the body effect
        else onAuthorized(authToken)
      }
      .fold(
        error => {
          log.error(s"Could not extract authorization token due to an error:", error)
          onUnauthorized("You are not authorized to perform this action.")
        },
        identity
      )

  }

}

private object KalixAuthorization {

  final private[iam] def extractAuthToken(context: ActionContext): Either[Throwable, AuthToken] = {
    // Rewrite the Kalix JWT claims from the metadata
    val claims = context.metadata.map(entry => (entry.key.replace("_kalix-jwt-claim-", "").toLowerCase, entry.value))

    // Go through each claim and add it to an initially empty `JwtClaim` record
    val jwtClaim = Try {
      claims.foldLeft(JwtClaim()) { case (claim, (key, value)) =>
        key match {
          case "iss" => claim.by(value)
          case "sub" => claim.about(value)
          case "aud" => claim.to(parseJsonArrayOrThrow(value).toSet)
          case "exp" => claim.expiresAt(value.toLong)
          case "nbf" => claim.startsAt(value.toLong)
          case "iat" => claim.issuedAt(value.toLong)
          case "jti" => claim.withId(value)
          case _     => claim + (key, value)
        }
      }
    }

    // Extract an `AuthToken` from the `JwtClaim` record
    jwtClaim.toEither.flatMap(AuthToken.fromClaim)
  }

  final private def parseJsonArrayOrThrow(rawJson: String): Seq[String] =
    parse(rawJson)
      .map { json =>
        json.asArray.map(_.map(_.noSpaces)).getOrElse(Seq(json.noSpaces))
      }
      .fold(throw _, identity)

  final private def getCurrentMethodName(): String = {
    val ste = Thread.currentThread().getStackTrace
    if (ste.isEmpty) "" else ste.last.getMethodName
  }

}
