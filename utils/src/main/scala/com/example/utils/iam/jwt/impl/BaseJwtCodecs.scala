package com.example.utils.iam.jwt.impl

import java.security.{PrivateKey, PublicKey}
import cats.effect._
import cats.syntax.all._
import com.example.utils.iam.jwt.{JsonWebToken, JwtCodec, JwtDecoder, JwtEncoder}
import com.example.utils.iam.model._
import io.circe.{Decoder, Encoder}
import pdi.jwt.{Jwt, JwtClaim}

import scala.util.control.NonFatal

abstract class BaseJwtEncoder[PrivKey <: PrivateKey](privateKey: PrivKey) extends JwtEncoder {

  /*
   * Implementation
   */
  final def encode[C <: IamPayload](
    token: JsonWebToken[C]
  )(implicit encoder: Encoder[JsonWebToken[C]]): IO[String] =
    (for {
      tokenJson    <- IO(encoder(token))
      encodedToken <- encodeJwtFromJson(tokenJson.noSpaces, privateKey)
    } yield encodedToken).onError { case NonFatal(error) => processEncodingError(error) }

  final def encodePayload[C](claim: JwtClaim, payload: C)(implicit encoder: Encoder[C]): IO[String] =
    (for {
      payloadJson  <- IO(encoder(payload))
      jwtPayload   <- IO.pure(claim + payloadJson.noSpaces)
      encodedToken <- encodeJwtFromJson(jwtPayload.toJson, privateKey)
    } yield encodedToken).onError { case NonFatal(error) => processEncodingError(error) }

  /**
    * Handles Jwt encoding errors.
    *
    * @note
    *   Override this function for custom error handling.
    */
  protected def processEncodingError(error: Throwable): IO[Unit] = IO.raiseError(error)

  /**
    * Creates the final JWT string (i.e. {Base64 encoded Json}.{Base64 encoded Json}.{Base64 encoded signature}) from
    * the input JWT claim raw JSON text.
    *
    * @note
    *   Override this function to customize how JWT tokens are created from raw JSON.
    */
  protected def encodeJwtFromJson(jsonText: String, privateKey: PrivKey) = IO(
    Jwt.encode(jsonText, privateKey, algorithm)
  )

}

trait BaseJwtDecoder[PubKey <: PublicKey] extends JwtDecoder {

  /*
   * Implementation
   */
  final def decode[C <: IamPayload](
    token: String
  )(implicit decoder: Decoder[JsonWebToken[C]]): IO[JsonWebToken[C]] =
    (for {
      claimJson <- extractAndValidateClaim(token)
      token     <- IO.fromEither(decoder.decodeJson(claimJson))
    } yield token).onError { case NonFatal(error) => processDecodingError(error) }

  final def decodePayload[P](token: String)(implicit decoder: Decoder[P]) =
    (for {
      payloadJson <- extractAndValidateClaim(token)
      payload     <- IO.fromEither(decoder.decodeJson(payloadJson))
    } yield payload).onError { case NonFatal(error) => processDecodingError(error) }

  /** The [[PubKey public key]] used by this decoder. */
  def publicKey: PubKey

  /**
    * Handles Jwt decoding errors.
    *
    * @note
    *   Override this function for custom error handling.
    */
  protected def processDecodingError(error: Throwable): IO[Unit] = IO.raiseError(error)

  /**
    * Decodes the JWT string, and parses the JSON claim from the JWT payload.
    *
    * @note
    *   Override this function to customize how decoded JSON from JWT claim tokens is generated.
    */
  protected def extractAndValidateClaim(token: String) =
    for {
      claimJsonText <- IO.fromEither(Jwt.decode(token, publicKey).toEither)
      json          <- IO.fromEither(io.circe.parser.parse(claimJsonText.toJson))
    } yield json

}

abstract class BaseJwtCodec[PubKey <: PublicKey, PrivKey <: PrivateKey](privateKey: PrivKey)
    extends BaseJwtEncoder[PrivKey](privateKey)
    with BaseJwtDecoder[PubKey]
    with JwtCodec
