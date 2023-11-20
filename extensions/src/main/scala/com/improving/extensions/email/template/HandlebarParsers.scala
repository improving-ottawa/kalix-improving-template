package com.improving.extensions.email.template

import scala.annotation.tailrec

trait HandlebarParsers {
  import HandlebarParsers._

  protected def parseHandlebarsLine(line: TextLine, readAhead: => LazyList[TextLine]): ParseResult[HandlebarsToken] = {
    foreachBlockStartRegex.findFirstMatchIn(line.text) match {
      case Some(regexMatch) =>
        val start                             = ForeachBlockStart(regexMatch.group(1), regexMatch.group(2))
        val remainingLineText: Option[String] = {
          val remainingText = line.text.replace(regexMatch.matched, "")
          if (remainingText.isBlank) None else Some(remainingText)
        }

        parseForeachSectionBody(line.lineNumber, remainingLineText, readAhead) match {
          case ParseSuccess(bodyLines) =>
            ParseSuccess(ForeachTemplate(start, bodyLines))
          case failure: ParseFailure   => failure
        }

      case None =>
        handlebarsCaptureRegex.findFirstMatchIn(line.text) match {
          case Some(_) => parseHandlebarsReplacements(line)
          case None    => ParseSuccess(TextOnly)
        }
    }
  }

  private def parseHandlebarsReplacements(textLine: TextLine): ParseResult[LineReplacements] = {
    @tailrec def parseLineRec(line: String, acc: List[Replacement]): ParseResult[List[Replacement]] = {
      def nameMatch = nameRegex.findFirstMatchIn(line).map { regexMatch =>
        val nextLine    = line.replace(regexMatch.matched, "")
        val replacement = Replacement(regexMatch.matched, regexMatch.group(1))
        (nextLine, replacement)
      }

      def propertyMatch = propertyRegex.findFirstMatchIn(line).map { regexMatch =>
        val nextLine    = line.replace(regexMatch.matched, "")
        val replacement = Replacement(regexMatch.matched, regexMatch.group(1))
        (nextLine, replacement)
      }

      val matched = nameMatch.orElse(propertyMatch)
      matched match {
        case Some((nextLine, token)) => parseLineRec(nextLine, token :: acc)
        case None if acc.nonEmpty    => ParseSuccess(acc.reverse)
        case None                    => ParseFailure(textLine.lineNumber, s"Invalid replacement expression")
      }
    }

    parseLineRec(textLine.text, List.empty) match {
      case ParseSuccess(list)    => ParseSuccess(LineReplacements(list))
      case failure: ParseFailure => failure
    }
  }

  private def parseForeachSectionBody(
    startLineNumber: Int,
    remainingLineText: Option[String],
    readAhead: LazyList[TextLine]
  ): ParseResult[List[TextLine]] = {
    @tailrec def parseRec(reader: LazyList[TextLine], acc: List[TextLine]): ParseResult[List[TextLine]] =
      reader match {
        case line #:: _ if foreachBlockEndRegex.findFirstIn(line.text).nonEmpty => ParseSuccess(acc.reverse)
        case line #:: next                                                      => parseRec(next, line :: acc)
        case _                                                                  => ParseFailure(startLineNumber, "no matching '{{/foreach}} block end found to terminate foreach block.")
      }

    val reader: LazyList[TextLine] = remainingLineText match {
      case Some(lineText) => LazyList(TextLine(startLineNumber, lineText)) ++ readAhead
      case None           => readAhead
    }

    parseRec(reader, List.empty)
  }

}

object HandlebarParsers {

  sealed trait HandlebarsToken

  case object TextOnly extends HandlebarsToken

  case class LineReplacements(replacements: List[Replacement]) extends HandlebarsToken

  case class ForeachBlockStart(blockPropertyName: String, iteratorName: String)

  case class ForeachTemplate(
    start: ForeachBlockStart,
    templateLines: List[TextLine]
  ) extends HandlebarsToken

  private val nameRegexExpr     = """([a-zA-Z]\w+)"""
  private val propertyRegexExpr = """(([a-zA-Z]\w+)(?:\.([a-zA-Z]\w+))*)"""

  private val handlebarsCaptureRegex = """\{\{\s*(.+)\s*}}""".r
  private val nameRegex              = handlebarsRegexExpr(nameRegexExpr).r
  private val propertyRegex          = handlebarsRegexExpr(propertyRegexExpr).r

  private val foreachStartRegexExpr = s"""#foreach\\s+$nameRegexExpr\\s*:\\s*$propertyRegexExpr"""
  private val foreachEndRegexExpr   = s"\\/foreach"

  private def handlebarsRegexExpr(captureExpr: String): String = s"\\{\\{\\s*$captureExpr\\s*}}"

  private val foreachBlockStartRegex = s"\\{\\{\\s*$foreachStartRegexExpr\\s*}}".r
  private val foreachBlockEndRegex   = handlebarsRegexExpr(foreachEndRegexExpr).r

}
