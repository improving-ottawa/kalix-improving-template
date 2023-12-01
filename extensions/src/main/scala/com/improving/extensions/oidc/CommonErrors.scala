package com.improving.extensions.oidc

import sttp.model.{StatusCode, Uri}

private[oidc] object CommonErrors {

  case class TimeoutOrServiceUnavailableError(uri: Uri)
    extends Error(s"Endpoint `$uri` timeout or service unavailable.")

  case class EndpointResponseNotJson(uri: Uri, respBody: String)
    extends Throwable(s"Endpoint `$uri` returned a non-Json response. Body: $respBody")

  case class EndpointInvalidResponse(uri: Uri, statusCode: Int, respBody: String)
    extends Throwable(s"Endpoint `$uri` returned an unexpected (code: $statusCode) response: $respBody")

}
