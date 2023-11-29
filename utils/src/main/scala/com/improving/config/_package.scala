package com.improving

import cats.data.Kleisli
import cats.effect.SyncIO
import com.typesafe

import scala.concurrent.duration._
import scala.util.Try

/**
  * `config` package object.
  *
  * @note
  *   Simply wildcard import this to use the configuration library.
  *   {{{
  *   import com.improving.config._
  *   }}}
  */
//noinspection ScalaFileName
package object config extends ConfigReaderExtensions {

  // Type Aliases
  final type Config          = typesafe.config.Config
  final type ConfigReader[R] = Kleisli[SyncIO, Config, R]

  // Config pseudo-companion
  object Config {

    /**
      * Gets an empty configuration.
      *
      * @return
      *   an empty configuration
      */
    final def empty: Config = typesafe.config.ConfigFactory.empty()
  }

  // ConfigReader pseudo-companion
  object ConfigReader {

    def apply[R](readK: Config => SyncIO[R]): ConfigReader[R] =
      Kleisli(cfg => SyncIO.defer(readK(cfg)))

    /**
      * Create a new [[ConfigReader]] that always returns the same value (does not actually read from any
      * configuration).
      */
    def always[R](value: R): ConfigReader[R] = apply(_ => SyncIO.pure(value))

    /** Create a new [[ConfigReader]], using a function from [[Config]] => [[R]]. */
    def readF[R](f: Config => R): ConfigReader[R] = apply(cfg => SyncIO.delay(f(cfg)))

    /**
      * Create a new [[ConfigReader]], using a Kleisli function from: [[Config configuration]] => [[Try effect of `R`]].
      */
    def readTry[R](f: Config => Try[R]): ConfigReader[R] =
      apply(cfg => SyncIO.fromTry(f(cfg)))

    /**
      * Create a new [[ConfigReader]], using a Kleisli function from: [[Config configuration]] =>
      * [[Either effect of `R`]].
      */
    def readEither[R](f: Config => Either[Throwable, R]): ConfigReader[R] =
      apply(cfg => SyncIO.fromEither(f(cfg)))

    /** Creates a function, which given a path string, produces a [[ConfigReader]]. */
    def pathInputReader[R](f: String => Config => R): String => ConfigReader[R] =
      path => readF(cfg => f(path)(cfg))

  }

  /** Builtin [[ConfigReader configuration readers]] */
  object readers extends BuiltinConfigReaders

  /** [[Config]] extension methods. */
  implicit class ConfigExtensions(private val config: Config) extends AnyVal {
    import scala.jdk.CollectionConverters._
    import java.util.concurrent.TimeUnit

    def getOrDefault[A](key: String, default: A): A =
      if (config.hasPath(key)) config.getAnyRef(key).asInstanceOf[A]
      else default

    def getStringOrDefault(key: String, default: String): String =
      if (config.hasPath(key)) config.getString(key)
      else default

    def getIntOrDefault(key: String, default: Int): Int =
      if (config.hasPath(key)) config.getInt(key)
      else default

    def getStringList(key: String): List[String] = config.getStringList(key).asScala.toList

    def getStringVector(key: String): Vector[String] = config.getStringList(key).asScala.toVector

    def getFiniteDuration(path: String): FiniteDuration =
      FiniteDuration(config.getDuration(path).toMillis, TimeUnit.MILLISECONDS)

  }

}
