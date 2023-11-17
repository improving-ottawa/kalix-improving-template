package com.example.gateway.api

import com.example.gateway.domain.{
  ClaimTokenFailure,
  ClaimTokenRequest,
  ClaimTokenResponse,
  ClaimTokenSuccess,
  CreateLoginTokenRequest,
  CreateLoginTokenResponse,
  LoginTokenState
}
import com.example.gateway.utils.GatewayKeyLoader
import com.example.utils.{JwtService, SystemClock}
import com.google.protobuf.empty.Empty
import kalix.scalasdk.valueentity.{ValueEntity, ValueEntityContext}

import java.time.Instant

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LoginTokenService(context: ValueEntityContext) extends AbstractLoginTokenService {
  import LoginTokenService._

  private type Effect[T] = ValueEntity.Effect[T]
  private val jwtService = JwtService(GatewayKeyLoader)
  import io.grpc.Status.{Code => StatusCode}

  private[this] var token: Option[String] = None

  override def emptyState: LoginTokenState = LoginTokenState.defaultInstance

  override def createLoginToken(
    currentState: LoginTokenState,
    req: CreateLoginTokenRequest
  ): Effect[CreateLoginTokenResponse] =
    token match {
      case Some(_) =>
        effects.error(s"Attempted to register a duplicate LoginToken: [${req.token}]", StatusCode.INVALID_ARGUMENT)

      case None =>
        val loginToken = req.token
        token = Some(loginToken)
        val newState   = LoginTokenState(
          // usage = req.usage,
          // field = req.field,
          // userEmail = req.userEmail,
          redirectUri = req.redirectUri,
          expirationTimestamp = Some(createExpirationTimestamp)
        )

        effects.updateState(newState).thenReply(CreateLoginTokenResponse(loginToken))
    }

  override def claimLoginToken(
    currentState: LoginTokenState,
    claimTokenRequest: ClaimTokenRequest
  ): Effect[ClaimTokenResponse] = {
    val nowTimestamp = SystemClock.currentInstant
    token match {
      case None    => createErrorResponse(s"Login token is invalid", StatusCode.NOT_FOUND)
      case Some(_) =>
        currentState.expirationTimestamp match {
          case Some(instant) if instant.compareTo(nowTimestamp) < 0 =>
            createErrorResponse(s"Login token has expired. Please login again.", StatusCode.OUT_OF_RANGE)

          case None    => createErrorResponse(s"Login token is invalid", StatusCode.NOT_FOUND)
          case Some(_) => createSuccessResponse(currentState)
        }
    }
  }

  private def createErrorResponse(message: String, code: io.grpc.Status.Code): Effect[ClaimTokenResponse] =
    effects.deleteEntity.thenReply(
      ClaimTokenResponse(
        ClaimTokenResponse.Response.Failure(
          ClaimTokenFailure(
            code.value(),
            message
          )
        )
      )
    )

  private def createSuccessResponse(currentState: LoginTokenState): Effect[ClaimTokenResponse] = {
    val token = jwtService.createAuthorizationToken(
      tokenIssuer,
//      currentState.userEmail,
      jwtTokenValidDuration,
      java.util.UUID.randomUUID().toString,
      Map(
//        "usage" -> currentState.usage.toString(),
        //      "field" -> currentState.field
      )
    )

    val responseProgram = token.map { jwt =>
      val response = ClaimTokenResponse(
        ClaimTokenResponse.Response.Success(
          ClaimTokenSuccess(
            // currentState.userEmail,
            // currentState.field,
            // currentState.usage,
            jwt
          )
        )
      )

      // Note: `deleteEntity` here because LoginTokens are single use only
      effects.deleteEntity.thenReply(response)
    }

    responseProgram.fold(
      error => effects.error(error.getMessage, StatusCode.INTERNAL),
      identity
    )
  }

  def invalidateLoginToken(currentState: LoginTokenState, claimTokenRequest: ClaimTokenRequest): Effect[Empty] =
    effects.deleteEntity.thenReply(Empty.of())

}

object LoginTokenService {
  final val jwtTokenValidDuration = java.time.Duration.ofHours(6)
  final val tokenValidDuration = java.time.Duration.ofHours(1)
  final val tokenIssuer = "example.io"

  private def createExpirationTimestamp: Instant = SystemClock.currentInstant
      .plus(tokenValidDuration)
      .plus(tokenValidDuration)

}
