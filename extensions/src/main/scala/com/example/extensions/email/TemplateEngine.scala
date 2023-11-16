package com.example.extensions.email

import cats.data.{NonEmptyChain, Validated}
import cats.effect._
import cats.effect.kernel.Resource
import cats.effect.unsafe.IORuntime
import cats.instances.all._
import cats.syntax.all._

import scala.concurrent._
import java.io._
import java.net.URI
import template._

sealed trait TemplateSource extends Iterator[String] with AutoCloseable {
  def templateUri: URI
}

sealed trait TemplateEngineSelectSource {
  def fromTemplateFile(templateFilePath: String): TemplateEngine
  def fromTemplateResource(templateResourcePath: String): TemplateEngine
  def fromTemplateString(templateText: String): TemplateEngine
}

private object TemplateEngineSelectSource {
  def apply(substitutionDataSources: Map[String, Any]): TemplateEngineSelectSource =
    Impl(substitutionDataSources)

  final private case class Impl(substitutionDataSources: Map[String, Any]) extends TemplateEngineSelectSource {

    def fromTemplateFile(templateFilePath: String): TemplateEngine = {
      val templateFile = new File(templateFilePath)
      if (!templateFile.exists()) { throw new FileNotFoundException(templateFilePath) }
      val templateResource = Resource.fromAutoCloseable(IO(new FileInputStream(templateFile))).map { fileInputStream =>
        new StreamedTemplate(templateFile.toURI, fileInputStream)
      }
      new TemplateEngine(templateResource, substitutionDataSources)
    }

    def fromTemplateResource(templateResourcePath: String): TemplateEngine = {
      val resourcePath = if (templateResourcePath.startsWith("/")) templateResourcePath else "/" + templateResourcePath
      val templateUri = getClass.getResource(resourcePath).toURI
      val templateResource =
        Resource.fromAutoCloseable(IO(getClass.getResourceAsStream(resourcePath))).map { inputStream =>
          new StreamedTemplate(templateUri, inputStream)
        }

      new TemplateEngine(templateResource, substitutionDataSources)
    }

    def fromTemplateString(templateText: String): TemplateEngine = {
      val templateResource = Resource.make(IO(new TextTemplate(templateText)))(res => IO(res.close()))
      new TemplateEngine(templateResource, substitutionDataSources)
    }

  }

  final private class StreamedTemplate(val templateUri: URI, source: InputStream) extends TemplateSource {
    private val reader = new BufferedReader(new InputStreamReader(source))
    private[this] var hasClosed = false
    private[this] var buffer: Option[String] = Option(reader.readLine())

    def hasNext: Boolean = buffer.nonEmpty

    def next(): String = {
      val currentLine = buffer.getOrElse(throw new java.io.IOException("Read past end of file"))
      buffer = Option(reader.readLine())
      if (buffer.isEmpty) {
        close()
      }
      currentLine
    }

    def close(): Unit = {
      hasClosed = true
      reader.close()
      source.close()
    }

  }

  final private class TextTemplate(templateText: String) extends TemplateSource {
    private val reader = new BufferedReader(new StringReader(templateText))
    private[this] var buffer: Option[String] = Option(reader.readLine())

    def templateUri: URI = URI.create("text://templateText")

    def hasNext: Boolean = buffer.nonEmpty

    def next(): String = {
      val currentLine = buffer.getOrElse(throw new java.io.IOException("Read past end of template"))
      buffer = Option(reader.readLine())
      if (buffer.isEmpty) {
        close()
      }
      currentLine
    }

    def close(): Unit = reader.close()
  }

}

final class TemplateEngine private[email] (
    templateResource: Resource[IO, TemplateSource],
    substitutionDataSources: Map[String, Any]
) {
  import TemplateEngine._
  import Validated.{Invalid, Valid}

  val formatLines: IO[Seq[String]] =
    templateResource.use { linesSource =>
      formatTemplate(linesSource)
        .flatMap {
          case Valid(lines) => IO.pure(lines)
          case Invalid(err) => IO.raiseError(errorsToException(err, linesSource.templateUri.toString))
        }
    }

  def formatLinesFuture(implicit executionContext: ExecutionContext): Future[Seq[String]] = {
    implicit val ioRuntime: IORuntime = IORuntime.builder().setCompute(executionContext, () => ()).build()
    formatLines.unsafeToFuture()
  }

  val formatToString: IO[String] =
    templateResource.use { linesSource =>
      formatTemplate(linesSource)
        .flatMap {
          case Valid(lines) => IO.pure(lines)
          case Invalid(err) => IO.raiseError(errorsToException(err, linesSource.templateUri.toString))
        }
        .map(lines => lines.mkString(System.lineSeparator))
    }

  def formatToStringFuture(implicit executionContext: ExecutionContext): Future[String] = {
    implicit val ioRuntime: IORuntime = IORuntime.builder().setCompute(executionContext, () => ()).build()
    formatToString.unsafeToFuture()
  }

  def formatToFile(outputFilePath: String): IO[Unit] =
    templateResource.use { linesSource =>
      val outputFile = new java.io.File(outputFilePath)
      if (outputFile.exists()) {
        java.nio.file.Files
          .write(outputFile.toPath, new Array[Byte](0), java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)
      }

      Resource.fromAutoCloseable(IO(new java.io.FileOutputStream(outputFile))).use { outputFileStream =>
        Resource.fromAutoCloseable(IO(new BufferedWriter(new OutputStreamWriter(outputFileStream)))).use {
          outputWriter =>
            formatTemplate(linesSource)
              .flatMap {
                case Valid(lines) => IO.pure(lines)
                case Invalid(err) => IO.raiseError(errorsToException(err, linesSource.templateUri.toString))
              }
              .map { lines =>
                lines.foreach { line =>
                  outputWriter.write(line)
                  outputWriter.newLine()
                }
                outputWriter.flush()
              }
        }
      }
    }

  def formatToFileFuture(outputFilePath: String)(implicit executionContext: ExecutionContext): Future[Unit] = {
    implicit val ioRuntime: IORuntime = IORuntime.builder().setCompute(executionContext, () => ()).build()
    formatToFile(outputFilePath).unsafeToFuture()
  }

  private def errorsToException(errors: NonEmptyChain[String], templateUri: String): Throwable = {
    val errorLines = errors.map(str => "\t" + str).mkString_("\n")
    new RuntimeException(s"Formatting template '$templateUri' failed due to the following:\n" + errorLines)
  }

  private def formatTemplate(source: Iterator[String]): IO[ValidatedLines] = IO {
    val fileContents = source.toList
    val parsingResult = for {
      sections <- TemplateParser(fileContents).toEither
      formattedLines <- TemplateFormatter(sections, substitutionDataSources).toEither
    } yield formattedLines

    Validated.fromEither(parsingResult)
  }

}

object TemplateEngine {

  private type ValidatedLines = Validated[NonEmptyChain[String], Seq[String]]

  def apply(substitutions: Map[String, Any]): TemplateEngineSelectSource = TemplateEngineSelectSource(substitutions)

}
