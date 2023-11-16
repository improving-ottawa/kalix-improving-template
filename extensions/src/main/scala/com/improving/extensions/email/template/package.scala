package com.improving.extensions.email

package object template {

  case class TextLine(lineNumber: Int, text: String)

  private[template] sealed trait ParseResult[+A]
  private[template] case class ParseSuccess[A](result: A) extends ParseResult[A]
  private[template] case class ParseFailure(lineNumber: Int, error: String) extends ParseResult[Nothing] {
    def toError: String = s"(line $lineNumber): $error"
  }

  private[template] case class Replacement(replacementText: String, propertyName: String)

}
