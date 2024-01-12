package com.improving.extensions.identity.crypto

import com.improving.config._
import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms._

/**
  * A configuration used by the [[KeyLoader]] for loading cryptographic keys when operating with Json Web Tokens (JWTs).
  *
  * @param jwtAlgorithm
  *   The [[JwtAsymmetricAlgorithm JWT asymmetric algorithm]] used to verify JWTs. Note: can be `either` a
  *   [[JwtRSAAlgorithm]] `or` a [[JwtECDSAAlgorithm]], and it __must__ match the algorithm used when the public/private
  *   keys were created.
  *
  * @param publicKeyFilePath
  *   The path to the __public__ key (`.pem` or `.der`) file.
  * @param privateKeyFilePath
  *   The path to the __private__ key (`.pem` or `.der`) file.
  * @param privateKeyPassword
  *   (Optional) A secret key used for decrypting the private key file.
  */
case class KeyLoaderConfig(
  /** The [[JwtAsymmetricAlgorithm JWT asymmetric algorithm]] used to verify JWTs. */
  jwtAlgorithm: JwtAsymmetricAlgorithm,

  /** The path to the __public__ key (`.pem` or `.der`) file. */
  publicKeyFilePath: String,

  /** The path to the __private__ key (`.pem` or `.der`) file. */
  privateKeyFilePath: String,

  /** (Optional) A secret key used for decrypting the private key file. */
  privateKeyPassword: Option[String]
)

object KeyLoaderConfig {

  /** [[ShowConfig]] instance for [[KeyLoaderConfig]] */
  implicit val showConfigForKeyLoaderConfig: ShowConfig[KeyLoaderConfig] =
    ShowConfig { cfg => printer =>
      printer
        .appendLine("KeyLoaderConfig:")
        .indent
        .appendLine(s"Jwt Algorithm:        ${cfg.jwtAlgorithm.name}")
        .appendLine(s"Public Key Path:      ${cfg.publicKeyFilePath}")
        .appendLine(s"Private Key Path:     ${cfg.privateKeyFilePath}")
        .appendLine(s"Private Key Password: ${cfg.privateKeyPassword.fold("not-set")(_ => "set")}")
        .outdent
        .newline
    }

  /** Read an instance of the [[KeyLoaderConfig]] record from a [[Config configuration]]. */
  def fromConfig(config: Config, maybePath: Option[String] = None): Either[Throwable, KeyLoaderConfig] = {
    import readers._

    val srcCfg = maybePath.fold(ConfigReader.always(config))(readers.getConfig)
    val reader = for {
      algo        <- getString("jwt-algorithm").map(JwtAlgorithm.fromString).map(_.asInstanceOf[JwtAsymmetricAlgorithm])
      pubKeyPath  <- getString("public-key-path")
      privKeyPath <- getString("private-key-path")
      privKeyPass <- getString("private-key-password").optionally
    } yield KeyLoaderConfig(algo, pubKeyPath, privKeyPath, privKeyPass)

    val readConfig = srcCfg.andThenRead(reader)
    readConfig.runToEither(config)
  }

}
