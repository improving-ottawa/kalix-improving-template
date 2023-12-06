package com.improving.extensions.oidc

import com.improving.utils._

/**
  * State that is stored during various OpenID Connect flows (such as "authorization token flow").
  *
  * @param key
  *   The unique session key, must be cryptographically random.
  * @param state
  *   The unique state that the requesting client sent us to preserve between redirect -> callback.
  */
final case class OIDCSession(key: Base64String, state: Base64String)

object OIDCSession {
  final def apply(state: Array[Byte]): OIDCSession = new OIDCSession(SecureRandomString(), Base64String(state))
}

/**
  * (Interface) Trait for a session store, which stores OIDC session information during "Authorization Code Flow".
  *
  * Information in this store __must__ be available to all Kalix service instances deployed with this functionality.
  * Said another way, all nodes must have access to the information in this store.
  *
  * @note
  *   This is not implemented for you because how OIDC sessions are stored for your specific application will depend on
  *   several factors, namely, which data storage options are available for storing the session information during
  *   "authorization token flow". <p /> Some example implementations could be:
  *   - Redis
  *   - PostgreSQL Table
  *   - Kalix "replicated entity"
  */
trait SessionStore[F[_]] {

  /** Puts a [[OIDCSession session]] into the session store. */
  def putSession(session: OIDCSession): F[OIDCSession]

  /** Retrieves (and removes) a [[OIDCSession session]] from the store for a given `key` if it exists. */
  def getSession(key: Base64String): F[Option[OIDCSession]]

}
