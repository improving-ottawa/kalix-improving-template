package com.improving.extensions.identity.password

import com.improving.extensions.identity.crypto.HMAC
import com.improving.utils.SecureString

import scala.util.Try

sealed trait PepperingSettings {
  def pepperingEnabled: Boolean
}

object PepperingSettings {
  // Preferring Blake3 to SHA256.
  // See: https://peergos.org/posts/blake3
  final val defaultHMACAlgorithm = "Blake3Mac"

  case object Disabled extends PepperingSettings {
    final val pepperingEnabled = false
  }

  def apply(privateKey: SecureString, hmacAlgorithm: String = defaultHMACAlgorithm): PepperingSettings =
    new Enabled(privateKey, hmacAlgorithm)

  final class Enabled(privateKey: SecureString, hmacAlgorithm: String) extends PepperingSettings {
    val pepperingEnabled = true
    def tryGetMac: Try[HMAC] = HMAC.create(privateKey, hmacAlgorithm)

  }

}
