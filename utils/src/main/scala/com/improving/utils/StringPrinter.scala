package com.improving.utils

import com.improving.config.ShowConfig

import cats.Show

/**
  * An enhanced, immutable/functional version of something akin to [[StringBuilder]], which can be used to create
  * complex formatted text using a variety of different printing/formatting functions.
  */
trait StringPrinter { self =>
  import StringPrinter.PrinterEndo

  override def toString: String = result

  def indentSize: Int
  def indentLevel: Int

  final def indent: StringPrinter = indent(1)
  def indent(level: Int): StringPrinter

  final def outdent: StringPrinter = outdent(1)

  def outdent(level: Int): StringPrinter

  def append(text: String): StringPrinter

  final def appendIf(predicate: => Boolean)(text: String): StringPrinter =
    if (predicate) append(text) else self

  def appendLine(first: String, rest: String*): StringPrinter
  def appendLine(seq: Seq[String]): StringPrinter

  final def appendLineIf(predicate: => Boolean)(first: String, rest: String*): StringPrinter =
    if (predicate) appendLine(first, rest: _*) else self

  final def appendLineStripMargin(first: String, rest: String*): StringPrinter =
    appendLine(first.stripMargin, rest.map(_.stripMargin): _*)

  def newline: StringPrinter

  final def when(predicate: => Boolean)(func: PrinterEndo): StringPrinter =
    if (predicate) func(self) else self

  final def ifThen(predicate: => Boolean)(ifTrue: PrinterEndo)(ifFalse: PrinterEndo): StringPrinter =
    if (predicate) ifTrue(self) else ifFalse(self)

  final def printEachNext[A](iterator: Iterator[A])(printf: (A, Boolean) => PrinterEndo): StringPrinter = {
    val elements  = iterator.toList
    val lastIndex = elements.length - 1
    elements.zipWithIndex.foldLeft(self) { case (printer, (element, index)) =>
      val hasNext = index != lastIndex
      printf(element, hasNext)(printer)
    }
  }

  final def printEach[A](elements: Iterable[A])(printf: A => PrinterEndo): StringPrinter =
    elements.foldLeft(self) { case (printer, element) => printf(element)(printer) }

  final def applyEndo(funcs: PrinterEndo*): StringPrinter =
    funcs.foldLeft(self)((printer, f) => f(printer))

  final def appendShow[A : Show](element: A): StringPrinter =
    appendLine(Show[A].show(element))

  final def appendConfig[Cfg : ShowConfig](config: Cfg): StringPrinter =
    applyEndo(implicitly[ShowConfig[Cfg]].print(config, _))

  def lines: Iterable[String]

  def result: String
}

object StringPrinter {
  final type PrinterEndo = StringPrinter => StringPrinter

  final val DefaultIndentSize = 2

  final val LineSeparator = System.lineSeparator()

  def apply(startingIndentLevel: Int = 0, indentSize: Int = DefaultIndentSize): StringPrinter =
    DefaultPrinter(None, Vector.empty, startingIndentLevel, indentSize)

  final private case class DefaultPrinter(
    currentLine: Option[String],
    content: Vector[String],
    indentLevel: Int = 0,
    indentSize: Int = DefaultIndentSize
  ) extends StringPrinter {
    private lazy val prefix = " " * (indentLevel * indentSize)

    def indent(level: Int): StringPrinter = copy(indentLevel = indentLevel + level)

    def outdent(level: Int): StringPrinter = copy(indentLevel = if (indentLevel == 0) 0 else indentLevel - level)

    def append(text: String): StringPrinter =
      currentLine match {
        case Some(line) => copy(Some(line + text))
        case None       => copy(Some(prefix + text))
      }

    def appendLine(first: String, rest: String*): StringPrinter = {
      val newContent = (first +: rest).flatMap(_.split(LineSeparator, 0))

      (currentLine, newContent) match {
        case (Some(line), head +: tail) => copy(None, (content :+ (line + head)) ++ tail.map(prefix + _))
        case (None, _)                  => copy(None, content ++ newContent.map(prefix + _))
        case (_, _)                     => this
      }
    }

    def appendLine(seq: Seq[String]): StringPrinter =
      if (seq.isEmpty) this
      else appendLine(seq.head, seq.tail: _*)

    def newline: StringPrinter =
      currentLine match {
        case Some(line) => copy(None, content :+ line)
        case None       => copy(None, content :+ "")
      }

    def lines: Iterable[String] = (content ++ currentLine).to(Iterable)

    def result: String = lines.mkString(LineSeparator)
  }

}
