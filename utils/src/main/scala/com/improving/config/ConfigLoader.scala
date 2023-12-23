package com.improving.config

import cats.syntax.all._
import com.typesafe.config._
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

import java.nio.file.{Path, Paths}

object ConfigLoader {

  /* Public API */

  /**
    * Load a required configuration from the filesystem.
    * @param filePath
    *   The filesystem path to the configuration file (either relative or absolute).
    */
  def loadRequiredFilesystemConfig(filePath: String, includeDefaultConfig: Boolean = true): Try[Config] =
    loadFileSystemConfig(filePath, required = true)
      .withFallbackConfig(includeDefaultConfig)
      .flatMap {
        case Some(config) => Success(config)
        case None         => Failure(MissingRequiredConfigurationError("file-system", filePath))
      }
      .handleErrorWith(err => Failure(ConfigResourceLoadException("file-system", err)))

  /**
    * Load an optional configuration from the filesystem.
    * @param filePath
    *   The filesystem path to the configuration file (either relative or absolute).
    */
  def loadOptionalFileSystemConfig(filePath: String, includeDefaultConfig: Boolean = true): Try[Config] =
    loadFileSystemConfig(filePath, required = false)
      .withFallbackConfig(includeDefaultConfig)
      .flatMap {
        case Some(config) => Success(config)
        case None         => Success(ConfigFactory.empty())
      }
      .handleErrorWith(err => Failure(ConfigResourceLoadException("file-system", err)))

  /**
    * Load a required configuration from a resource.
    * @param resourcePath
    *   The resource path (resource like path) of the configuration file.
    */
  def loadRequiredResourceConfig(resourcePath: String, includeDefaultConfig: Boolean = true): Try[Config] =
    loadResourceConfig(resourcePath, required = true)
      .withFallbackConfig(includeDefaultConfig)
      .flatMap {
        case Some(config) => Success(config)
        case None         => Failure(MissingRequiredConfigurationError("resource", resourcePath))
      }
      .handleErrorWith(err => Failure(ConfigResourceLoadException("resource", err)))

  /**
    * Load an optional configuration from a resource.
    * @param resourcePath
    *   The resource path (resource like path) of the configuration file.
    */
  def loadOptionalResourceConfig(resourcePath: String, includeDefaultConfig: Boolean = true): Try[Config] =
    loadResourceConfig(resourcePath, required = false)
      .withFallbackConfig(includeDefaultConfig)
      .flatMap {
        case Some(config) => Success(config)
        case None         => Success(ConfigFactory.empty())
      }
      .handleErrorWith(err => Failure(ConfigResourceLoadException("resource", err)))

  /* Implementation */

  implicit private class LoadTryExtensions(private val instance: Try[Option[Config]]) extends AnyVal {

    def withFallbackConfig(enabled: Boolean): Try[Option[Config]] =
      if (!enabled) instance
      else {
        instance.flatMap {
          case Some(config) => Try(ConfigFactory.load()).map(fallback => Some(config.withFallback(fallback)))
          case None         => Success(None)
        }
      }

  }

  final private case class MissingRequiredConfigurationError(resourceType: String, resourcePath: String)
      extends Error(s"Could not load required $resourceType configuration file from: $resourcePath")

  final private case class ConfigResourceLoadException(resourceType: String, cause: Throwable)
      extends Error(s"Could not load required $resourceType configuration file due to an error:", cause)

  private lazy val workingDirectory: Path = {
    import java.nio.file._

    val markerFile = "build.sbt"
    val userDir    = sys.props("user.dir")

    @tailrec def findRoot(path: Path, filename: String): Path =
      if (path.toFile.listFiles.exists(_.getName == filename)) path
      else findRoot(path.getParent, filename)

    findRoot(Path.of(userDir), markerFile)
  }

  private val log = LoggerFactory.getLogger("com.improving.config.ConfigLoader")

  final private def loadFileSystemConfig(filePath: String, required: Boolean): Try[Option[Config]] = {
    val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)
    val adjective    = if (required) "required" else "optional"

    def parseFile(fileStr: String): Try[Option[Config]] = {
      val configPath = Paths.get(fileStr)
      val finalPath  = if (configPath.isAbsolute) configPath else workingDirectory.resolve(configPath)
      val pathString = finalPath.toAbsolutePath.toFile.toString

      Try(ConfigFactory.parseFile(finalPath.toFile, parseOptions))
        .flatTap(cfg => Try(log.info(s"Loaded $adjective configuration file: $pathString")))
        .map(cfg => Option(cfg))
        .onError { case _: Throwable => reportCouldNotLoad(adjective, "file-system", filePath) }
    }

    parseFile(filePath)
  }

  final private def loadResourceConfig(resourceFilename: String, required: Boolean): Try[Option[Config]] = {
    val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)
    val adjective    = if (required) "required" else "optional"

    Try(ConfigFactory.parseResources(resourceFilename, parseOptions))
      .flatTap(cfg => Try(log.info(s"Loaded $adjective resource configuration file: $resourceFilename")))
      .map(cfg => Option(cfg))
      .onError { case _: Throwable => reportCouldNotLoad(adjective, "resource", resourceFilename) }
  }

  final private def reportCouldNotLoad(adjective: String, configType: String, resourcePath: String): Try[Unit] = {
    val errorMsg = s"Failed to load $adjective $configType configuration file from: $resourcePath"

    Try {
      if (adjective == "required")
        log.error(errorMsg)
      else
        log.debug(errorMsg)
    }
  }

}
