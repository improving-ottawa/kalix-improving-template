package com.improving.iam

import pdi.jwt.algorithms._

import java.security._
import java.security.interfaces._

sealed abstract class AlgorithmWithKeys {
  type Public <: PublicKey
  type Private <: PrivateKey
  type Algorithm <: JwtAsymmetricAlgorithm

  def publicKey: Public
  def privateKey: Private
  def algorithm: Algorithm
}

sealed trait TypedKeyPair[Pub <: PublicKey, Priv <: PrivateKey, Algo <: JwtAsymmetricAlgorithm] {
  self: AlgorithmWithKeys =>
  final override type Public    = Pub
  final override type Private   = Priv
  final override type Algorithm = Algo
}

object NoKeysPair extends AlgorithmWithKeys with TypedKeyPair[PublicKey, PrivateKey, JwtAsymmetricAlgorithm] {
  final def publicKey: PublicKey = throw new RuntimeException("No public key loaded.")

  final def privateKey: PrivateKey = throw new RuntimeException("No private key loaded.")

  final def algorithm: JwtAsymmetricAlgorithm = throw new RuntimeException("No JWT algorithm specified.")
}

final case class RSAKeyPair(publicKey: RSAPublicKey, privateKey: RSAPrivateKey, algorithm: JwtRSAAlgorithm)
    extends AlgorithmWithKeys
    with TypedKeyPair[RSAPublicKey, RSAPrivateKey, JwtRSAAlgorithm]

final case class ECKeyPair(publicKey: ECPublicKey, privateKey: ECPrivateKey, algorithm: JwtECDSAAlgorithm)
    extends AlgorithmWithKeys
    with TypedKeyPair[ECPublicKey, ECPrivateKey, JwtECDSAAlgorithm]
