package com.example.utils

import scala.util.{Try, Success}

import java.security.{PrivateKey, PublicKey}

abstract class KeyLoader {
  import KeyLoader._

  @volatile private[this] var state: State = Empty

  final def privateKey: Try[PrivateKey] =
    state match {
      case Cached(privateKey, _) => Success(privateKey)
      case Empty                 => loadAndCacheKeys.map(_.privateKey)
    }

  final def publicKey: Try[PublicKey] =
    state match {
      case Cached(_, publicKey) => Success(publicKey)
      case Empty                => loadAndCacheKeys.map(_.publicKey)
    }

  protected def loadPrivateKey(): PrivateKey
  protected def loadPublicKey(): PublicKey

  private def loadAndCacheKeys: Try[Cached] =
    synchronized {
      state match {
        case cached@Cached(_, _) => Success(cached)
        case Empty               => Try {
          val privateKey = loadPrivateKey()
          val publicKey = loadPublicKey()
          val cached = Cached(privateKey, publicKey)
          state = cached
          cached
        }
      }
    }

}

object KeyLoader {

  private sealed trait State
  private case object Empty extends State
  private case class Cached(privateKey: PrivateKey, publicKey: PublicKey) extends State

}
