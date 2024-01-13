package com.improving.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.security.{Provider, Security}

object BouncyCastle {

  // Bouncy Castle provider instance
  final val bcProviderInstance: Provider = new BouncyCastleProvider

  final def providerName: String = bcProviderInstance.getName

  /** Registers the BouncyCastle cryptographic provider (if not already registered) */
  final def register(): Unit =
    synchronized {
      val providerName = bcProviderInstance.getName
      if (Option(Security.getProvider(providerName)).isEmpty) {
        Security.addProvider(bcProviderInstance)
      }
    }

}
