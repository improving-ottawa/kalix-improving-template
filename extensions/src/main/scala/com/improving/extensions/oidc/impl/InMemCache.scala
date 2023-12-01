package com.improving.extensions.oidc.impl

import com.improving.utils.SystemClock
import cats.effect._
import scalacache._
import scalacache.caffeine._
import com.github.benmanes.caffeine.cache.Caffeine

import scala.concurrent._
import scala.concurrent.duration._

/** A shared / global application-wide cache for OIDC components. */
private[oidc] sealed trait InMemCache[F[_]] {

  def getValue[T <: AnyRef](key: String): F[Option[T]]

  def putValue[T <: AnyRef](key: String, value: T): F[Unit]

}

private[oidc] object InMemCache {
  private final val cachingDuration: Duration = FiniteDuration(5, "minutes")
  private val cachingJavaDuration = java.time.Duration.of(cachingDuration.length, cachingDuration.unit.toChronoUnit)

  /* Public API */

  def catsEffect: InMemCache[IO] = CatsEffectCache

  def scalaFuture(implicit ec: ExecutionContext): InMemCache[Future] = new ScalaFutureCache

  // A single shared (per service) global cache is shared across all of the various OIDC client instances.
  private final lazy val underlyingCache = Caffeine.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(cachingDuration.length, cachingDuration.unit)
    .build[String, Entry[AnyRef]]()

  /** Cats Effect based implementation of [[InMemCache]] */
  private object CatsEffectCache extends InMemCache[IO] {
    // This cache instance just wraps the underlying caffeine cache
    private implicit lazy val cache: Cache[IO, String, AnyRef] = new CaffeineCache(underlyingCache)

    def getValue[T <: AnyRef](key: String): IO[Option[T]] =
      cache.get(key).map {
        case Some(value) => Some(value.asInstanceOf[T])
        case None        => None
      }

    def putValue[T <: AnyRef](key: String, value: T): IO[Unit] =
      cache.put(key)(value, Some(cachingDuration))

  }

  /** Scala [[Future]] based implementation of [[InMemCache]] */
  private final class ScalaFutureCache(implicit ec: ExecutionContext) extends InMemCache[Future] {
    def getValue[T <: AnyRef](key: String): Future[Option[T]] =
      Future {
        val entry = underlyingCache.getIfPresent(key)
        if (entry == null) None
        else Some(entry.value.asInstanceOf[T])
      }

    def putValue[T <: AnyRef](key: String, value: T): Future[Unit] = {
      val expiresInstant = SystemClock.currentInstant.plus(cachingJavaDuration)
      val entry: Entry[AnyRef] = Entry(value, Some(expiresInstant))

      Future(underlyingCache.put(key, entry))
    }
  }

}
