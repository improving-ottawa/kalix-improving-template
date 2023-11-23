package com.example.utils.iam.jwt

import io.circe._
import io.circe.syntax._
import pdi.jwt.JwtAlgorithm

case class IamJwtHeader(algorithm: JwtAlgorithm)

object IamJwtHeader {
  final implicit val toJson: Encoder[IamJwtHeader] = (header: IamJwtHeader) =>
    Json.obj(
      "typ" -> "JWT".asJson,
      "alg" -> header.algorithm.name.asJson
    )

  final implicit val fromJson: Decoder[IamJwtHeader] = (c: HCursor) =>
    for {
      alg <- c.get[String]("alg").map(JwtAlgorithm.fromString)
      _   <- c.get[String]("typ").map(typ => require(typ == "JWT", "Token type is not JWT"))
    } yield IamJwtHeader(alg)
}
