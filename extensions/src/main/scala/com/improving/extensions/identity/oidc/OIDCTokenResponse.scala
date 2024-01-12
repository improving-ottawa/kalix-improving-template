package com.improving.extensions.identity.oidc

import io.circe._
import io.circe.syntax._

/**
  * An OIDC token response, which should contain JSON comprising of the OIDCUser.
  *
  * @param accessToken
  *   The access token received from the provider (ignored for now)
  * @param refreshToken
  *   The refresh token received from the provider (ignored for now)
  * @param idToken
  *   The identity token received from the provider (contains user information)
  */
final case class OIDCTokenResponse(accessToken: Option[String], refreshToken: Option[String], idToken: String)

object OIDCTokenResponse {

  /** Circe [[Decoder decoder]] from a [[Json JSON]] object to [[OIDCTokenResponse]]. */
  implicit final val fromJson: Decoder[OIDCTokenResponse] =
    (c: HCursor) =>
      for {
        accToken <- c.get[Option[String]]("access_token")
        refToken <- c.get[Option[String]]("refresh_token")
        idToken  <- c.get[String]("id_token")
      } yield OIDCTokenResponse(accToken, refToken, idToken)

  /** Circe [[Encoder encoder]] from [[OIDCIdentity]] to a [[Json JSON]] object. */
  implicit final val toJson: Encoder[OIDCTokenResponse] =
    (tr: OIDCTokenResponse) =>
      Json.obj(
        "access_token"  -> tr.accessToken.asJson,
        "refresh_token" -> tr.refreshToken.asJson,
        "id_token"      -> tr.idToken.asJson
      )

}
