package com.example.extensions.email.template

import cats.data.{NonEmptyChain, Validated}

import scala.annotation.tailrec

sealed trait TemplateSection

object TemplateSection {
  case class TextSection(lines: List[String])                                       extends TemplateSection
  case class HandlebarsSection(textLine: TextLine, replacements: List[Replacement]) extends TemplateSection

  case class ForeachSection(
    startLine: TextLine,
    blockPropertyName: String,
    iteratorName: String,
    nestedSections: List[TemplateSection]
  ) extends TemplateSection

}

sealed trait TemplateParser extends HandlebarParsers {
  import TemplateParser._
  import HandlebarParsers._

  final def apply(source: Iterable[String]): ParsingResult[TemplateSection] = {
    val textLineSource = source.view.zipWithIndex
      .map { case (line, index) => TextLine(index + 1, line) }
      .to(Iterable)

    val initialContext = new ParsingContext(textLineSource)
    parseSections(initialContext, List.empty, List.empty, List.empty)
  }

  final private def parsingSuccess[A](value: List[A]): ParsingResult[A] = Validated.valid(value)

  final private def parsingFailure(error: String, otherErrors: Iterable[String]): ParsingResult[Nothing] =
    Validated.invalid(NonEmptyChain(error, otherErrors.toSeq: _*))

  private def parseNestedSections(lines: Iterable[TextLine]): ParsingResult[TemplateSection] = {
    val nestedContext = new ParsingContext(lines)
    parseSections(nestedContext, List.empty, List.empty, List.empty)
  }

  @tailrec private def parseSections(
    context: ParsingContext,
    textLines: List[String],
    sections: List[TemplateSection],
    errors: List[String]
  ): ParsingResult[TemplateSection] = {
    import TemplateSection._

    context.nextLine match {
      case None if textLines.nonEmpty =>
        parseSections(context, List.empty, TextSection(textLines.reverse) :: sections, errors)

      case None =>
        errors.reverse match {
          case head :: tail            => parsingFailure(head, tail)
          case Nil if sections.isEmpty => Validated.invalid(NonEmptyChain.one("Template contains no content."))
          case Nil                     => parsingSuccess(sections.reverse)
        }

      case Some(textLine) =>
        parseHandlebarsLine(textLine, context.readAhead) match {
          case ParseSuccess(TextOnly) => parseSections(context, textLine.text :: textLines, sections, errors)

          case ParseSuccess(LineReplacements(replacements)) =>
            if (textLines.nonEmpty) {
              val textSection        = TextSection(textLines.reverse)
              val replacementSection = HandlebarsSection(textLine, replacements)
              parseSections(context, List.empty, replacementSection :: textSection :: sections, errors)
            } else {
              val replacementSection = HandlebarsSection(textLine, replacements)
              parseSections(context, List.empty, replacementSection :: sections, errors)
            }

          case ParseSuccess(ForeachTemplate(start, templateLines)) =>
            val nextSections = if (textLines.nonEmpty) {
              val textSection = TextSection(textLines.reverse)
              textSection :: sections
            } else sections

            val nestedSectionsResult = parseNestedSections(templateLines)
            nestedSectionsResult match {
              case failure @ Validated.Invalid(_)  => failure
              case Validated.Valid(nestedSections) =>
                val foreachSection =
                  ForeachSection(textLine, start.blockPropertyName, start.iteratorName, nestedSections)
                context.skipLines(templateLines.length + 1)
                parseSections(context, List.empty, foreachSection :: nextSections, errors)
            }

          case error @ ParseFailure(_, _) => parseSections(context, textLines, sections, error.toError :: errors)
        }
    }
  }

}

object TemplateParser extends TemplateParser {
  final private type ParsingResult[A] = Validated[NonEmptyChain[String], List[A]]

  private class ParsingContext(source: Iterable[TextLine]) {
    private val lineIterator: Iterator[TextLine] = source.iterator
    private[this] var readLines                  = 0

    def nextLine: Option[TextLine] =
      if (lineIterator.hasNext) {
        readLines += 1
        Some(lineIterator.next())
      } else None

    def skipLines(n: Int): Unit = (1 to n).foreach { _ =>
      if (lineIterator.hasNext) { lineIterator.next() }
      ()
    }

    def readAhead: LazyList[TextLine] = LazyList.from(source.drop(readLines))
  }

}
