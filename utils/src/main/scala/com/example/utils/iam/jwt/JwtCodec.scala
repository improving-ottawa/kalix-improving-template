package com.example.utils.iam.jwt

import java.security.PublicKey
import java.security.interfaces.{ECPrivateKey, ECPublicKey, RSAPrivateKey, RSAPublicKey}
import pdi.jwt.algorithms._
import cats.effect.IO
import com.example.utils.iam.cryptography.{AlgorithmWithKeys, ECKeyPair, RSAKeyPair}
import com.example.utils.iam.model.IamPayload
import io.circe.{Decoder, Encoder}
import pdi.jwt.JwtClaim

/**
  * Base JWT encoder.
  */
trait JwtEncoder {

  /** The [[JwtAsymmetricAlgorithm JWT asymmetric algorithm]] used when signing tokens. */
  def algorithm: JwtAsymmetricAlgorithm

  /**
    * Converts the [[JsonWebToken[C] Jwt]] instance into JSON, signs it with the `privateKey` using the specified
    * `algorithm`, then builds the final output JWT string.
    */
  def encode[C <: IamPayload](token: JsonWebToken[C])(implicit encoder: Encoder[JsonWebToken[C]]): IO[String]

  /**
    * Encodes the provided [[C payload]] into JSON, combines that with the provided [[JwtClaim Jwt claim]], signs it
    * with the `privateKey` using the defined `algorithm`, then builds the final output JWT string.
    */
  def encodePayload[C](claim: JwtClaim, payload: C)(implicit encoder: Encoder[C]): IO[String]
}

/**
  * Base JWT decoder.
  */
trait JwtDecoder {

  /** The allowed Jwt signing algorithms. */
  def allowedAlgorithms: Seq[JwtAsymmetricAlgorithm]

  /**
    * Decodes the [[JsonWebToken[C] token]], provided a raw JWT token string.
    * @note
    *   The type parameter [[C]] must be specified when calling this function.
    */
  def decode[C <: IamPayload](token: String)(implicit decoder: Decoder[JsonWebToken[C]]): IO[JsonWebToken[C]]

  /** Decodes just the [[P payload]] from a raw JWT token string. */
  def decodePayload[P](token: String)(implicit decoder: Decoder[P]): IO[P]
}

/**
  * Companion for JwtDecoder. Has global methods for creating new decoders.
  */
object JwtDecoder {

  /**
    * Creates a new [[JwtDecoder JWT decoder]] given a supported [[PublicKey public key]].
    */
  final def apply(publicKey: PublicKey): IO[JwtDecoder] =
    publicKey match {
      case rsaKey: RSAPublicKey  => IO.pure(rsa(rsaKey))
      case ecdsaKey: ECPublicKey => IO.pure(ecdsa(ecdsaKey))
      case other                 =>
        IO.raiseError(
          new NotImplementedError(s"Provided `PublicKey` type (${other.getClass.getName}) is not supported.")
        )
    }

  /**
    * Creates a new RSA Jwt Decoder using the specified `publicKey`.
    *
    * @param publicKey
    *   The [[RSAPublicKey RSA public key]] to use when decoding JWTs.
    */
  final def rsa(publicKey: RSAPublicKey): JwtDecoder = impl.RSADecoder(publicKey)

  /**
    * Creates a new Elliptic-Curve Jwt Decoder using the specified `publicKey`.
    *
    * @param publicKey
    *   The [[ECPublicKey EC public key]] to use when decoding JWTs.
    */
  final def ecdsa(publicKey: ECPublicKey): JwtDecoder = impl.ECDecoder(publicKey)
}

/**
  * Combined [[JwtEncoder encoder]] and [[JwtDecoder decoder]] for JWTs.
  */
trait JwtCodec extends JwtEncoder with JwtDecoder

/** EndMarker */
object JwtCodec {

  /**
    * Creates a new [[JwtCodec Jwt codec]] given an [[AlgorithmWithKeys algorithm and keys]].
    */
  final def fromAlgorithmWithKeys(algoWithKeys: AlgorithmWithKeys): IO[JwtCodec] =
    algoWithKeys match {
      case RSAKeyPair(publicKey, privateKey, algorithm) => IO.pure(rsa(publicKey, privateKey, algorithm))
      case ECKeyPair(publicKey, privateKey, algorithm)  => IO.pure(ecdsa(publicKey, privateKey, algorithm))
    }

  /**
    * Creates a new RSA Jwt Codec using the specified `publicKey`, `privateKey`, and RSA `algorithm`.
    *
    * @param publicKey
    *   The [[RSAPublicKey RSA public key]] to use when verifying JWTs.
    * @param privateKey
    *   The [[RSAPrivateKey RSA private key]] to use when signing JWTs.
    * @param algorithm
    *   The [[JwtRSAAlgorithm RSA algorithm]] to use when signing JWTs.
    */
  final def rsa(publicKey: RSAPublicKey, privateKey: RSAPrivateKey, algorithm: JwtRSAAlgorithm): JwtCodec =
    impl.RSACodec(privateKey, publicKey, algorithm)

  /**
    * Creates a new RSA Jwt Codec using the specified `algoWithKeys`.
    *
    * @param algoWithKeys
    *   The [[RSAKeyPair algorithm and keys]] to use for the RSA codec.
    */
  final def rsa(algoWithKeys: RSAKeyPair): JwtCodec =
    rsa(algoWithKeys.publicKey, algoWithKeys.privateKey, algoWithKeys.algorithm)

  /**
    * Creates a new Elliptic-Curve Jwt Codec using the specified `publicKey`, `privateKey`, and RSA `algorithm`.
    *
    * @param publicKey
    *   The [[ECPublicKey EC public key]] to use when verifying JWTs.
    * @param privateKey
    *   The [[ECPrivateKey EC private key]] to use when signing JWTs.
    * @param algorithm
    *   The [[JwtECDSAAlgorithm EC algorithm]] to use when signing JWTs.
    */
  final def ecdsa(publicKey: ECPublicKey, privateKey: ECPrivateKey, algorithm: JwtECDSAAlgorithm): JwtCodec =
    impl.ECCodec(privateKey, publicKey, algorithm)

  /**
    * Creates a new Elliptic-Curve Jwt Codec using the specified `algoWithKeys`.
    *
    * @param algoWithKeys
    *   The [[ECKeyPair algorithm and keys]] to use for the Elliptic-Curve codec.
    */
  final def ecdsa(algoWithKeys: ECKeyPair): JwtCodec =
    ecdsa(algoWithKeys.publicKey, algoWithKeys.privateKey, algoWithKeys.algorithm)

}
