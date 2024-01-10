package com.improving.extensions.identity

import com.improving.utils.Base64String

import javax.crypto.Mac
import scala.util.Try

sealed trait PepperingSettings {
  def pepperingEnabled: Boolean
}

object PepperingSettings {

  // Load the BouncyCastle provider on class instantiation
  com.improving.utils.BouncyCastle.register()

  case object Disabled extends PepperingSettings {
    final val pepperingEnabled = false
  }

  final case class Enabled(hmacAlgorithm: String, key: Base64String) extends PepperingSettings {
    val pepperingEnabled    = false
    def tryGetMac: Try[Mac] = Try(Mac.getInstance(hmacAlgorithm))
  }

}
