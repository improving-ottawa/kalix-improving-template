package com.example.gateway.api

import com.example.common.api.JwtAuthorization
import com.example.gateway.domain._
import kalix.scalasdk.action.Action

/** EndMarker */
trait LoginProxy extends GatewayProxyBase with JwtAuthorization {

  // TODO: uncommnent if enabling email
  // def sendAdminLoginLink(sendLoginLinkRequest: SendLoginLinkRequest): Action.Effect[SendLoginLinkResponse] = {
  //  val userEmail = sendLoginLinkRequest.userEmail
//
  //  effects.asyncReply(
  //    TODO: if needed, send a request for admin details and then use validateEmailDomainThenSend on success
  //    On failure of admin details request, log a warning and reply with SendLoginLinkResponse.defaultInstance
  //
  //    sendLoginLink(Left(userEmail), LoginTokenUsage.Admin)
  //  )
  // }

  def claimLoginToken(claimTokenRequest: ClaimTokenRequest): Action.Effect[ClaimTokenResponse] =
    effects.forward(components.loginTokenService.claimLoginToken(claimTokenRequest))

  def validateJwt(req: JwtValidationRequest): Action.Effect[JwtValidationResponse] = {
    // val authorization = extractJwtAuthorization()
    // TODO: add validation here based on authorization

    effects.reply(JwtValidationResponse(isValid = false))
  }

}
