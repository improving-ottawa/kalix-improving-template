package com.improving.config

import cats.effect.SyncIO

import scala.language.implicitConversions

trait ConfigReaderExtensions {
  import ConfigReaderExtensions._

  implicit final def readerExtensions[R](reader: ConfigReader[R]): ConfigReaderCombinators[R] =
    new ConfigReaderCombinators(reader)

  implicit final def readerOptionExtensions[R](reader: ConfigReader[Option[R]]): ConfigReaderOptionCombinators[R] =
    new ConfigReaderOptionCombinators(reader)

  implicit final def readerConfigExtensions(reader: ConfigReader[Config]): ConfigReaderOfConfigExtensions =
    new ConfigReaderOfConfigExtensions(reader)

}

object ConfigReaderExtensions extends ConfigReaderExtensions {

  /** Extension methods for [[ConfigReader]]. */
  final class ConfigReaderCombinators[R](private val reader: ConfigReader[R]) extends AnyVal {

    /** Runs this `reader`, yielding [[Either either]] the `result`, or the configuration read [[Throwable error]]. */
    def runToEither(config: Config): Either[Throwable, R] = reader(config).attempt.unsafeRunSync()

    /**
      * Convert this reader into one which __optionally__ reads the [[Config configuration]], yielding [[Some]] if the
      * configuration value was read, and [[None]] if it was not.
      */
    def optionally: ConfigReader[Option[R]] =
      ConfigReader { cfg =>
        reader.run(cfg).map(Option.apply).handleErrorWith(_ => SyncIO.pure(None))
      }

    /**
      * Creates a reader which will attempt this reader first, and if that fails, will instead try the provided
      * `orElseReader`.
      */
    def orElseRead[A >: R](orElseReader: ConfigReader[A]): ConfigReader[A] =
      ConfigReader { cfg =>
        reader.run(cfg).map(x => x: A).handleErrorWith(_ => orElseReader.run(cfg))
      }

    /** Converts this reader into one which will yield the provided `defaultValue` if reading fails. */
    def withDefault[A >: R](defaultValue: => A): ConfigReader[A] =
      ConfigReader { cfg =>
        reader.run(cfg).map(x => x: A).handleErrorWith(_ => SyncIO.delay(defaultValue))
      }

  }

  /**
    * Extension methods for [[ConfigReader]] (option reader combinators).
    */
  final class ConfigReaderOptionCombinators[R](private val reader: ConfigReader[Option[R]]) extends AnyVal {

    def mapSome[A](f: R => A): ConfigReader[Option[A]] =
      reader.flatMapF {
        case Some(value) => SyncIO.delay(Some(f(value)))
        case _           => SyncIO.pure(None)
      }

    def emptyStringToNone(implicit ev: R =:= String): ConfigReader[Option[R]] =
      reader.map {
        case Some(str) if str.isEmpty => None
        case None                     => None
        case ok @ Some(_)             => ok
      }

  }

  /**
    * Extension methods for [[ConfigReader]] of [[Config]]
    */
  final class ConfigReaderOfConfigExtensions(private val cfgReader: ConfigReader[Config]) extends AnyVal {
    def andThenRead[R](reader: ConfigReader[R]): ConfigReader[R] = cfgReader.flatMapF(reader.run)
  }

}
