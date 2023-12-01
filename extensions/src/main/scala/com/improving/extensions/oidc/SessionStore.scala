package com.improving.extensions.oidc

import com.improving.utils._

/**
  * State that is stored between redirect -> callback of OIDC provider.
  *
  * @param key   The unique session key, must be cryptographically random.
  * @param state The unique state that the requesting client sent us to preserve between redirect -> callback.
  */
final case class OIDCSession(key: Base64String, state: Base64String)

object OIDCSession {
  final def apply(state: Array[Byte]): OIDCSession = new OIDCSession(SecureRandomString(), Base64String(state))
}

/** (Interface) Trait for a session store, which stores OIDC session information during "Authorization Code Flow". */
trait SessionStore[F[_]] {

  /** Puts a [[OIDCSession session]] into the session store. */
  def putSession(session: OIDCSession): F[OIDCSession]

  /** Retrieves (and removes) a [[OIDCSession session]] from the store for a given `key` if it exists. */
  def getSession(key: Base64String): F[Option[OIDCSession]]

}
