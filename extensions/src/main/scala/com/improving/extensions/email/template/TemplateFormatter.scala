package com.improving.extensions.email.template

import cats.data.{NonEmptyChain, Validated}

import scala.annotation.tailrec

object TemplateFormatter {
  import TemplateSection._
  import TemplateSubstitutions._

  private type ValidatedLines = Validated[NonEmptyChain[String], Seq[String]]
  private type Validation[A] = Validated[NonEmptyChain[String], A]

  final def apply(sections: List[TemplateSection], substitutions: Map[String, Any]): ValidatedLines = {
    val subsMap = substitutionsMap(substitutions)
    val fmtSection = formatSection(_, subsMap)
    combineSectionResults(sections map fmtSection)
  }

  private def formatSection(section: TemplateSection, map: SubstitutionMap): ValidatedLines =
    section match {
      case txt: TextSection              => formatTextSection(txt)
      case handlebars: HandlebarsSection => formatHandlebarsSection(handlebars, map).map(str => Seq(str))
      case foreach: ForeachSection       => formatForeachSection(foreach, map)
    }

  private def formatTextSection(section: TextSection): ValidatedLines =
    Validated.valid(section.lines)

  private def formatHandlebarsSection(section: HandlebarsSection, map: SubstitutionMap): Validation[String] = {
    val lineNumber = section.textLine.lineNumber

    @tailrec def formatLineRec(line: String, replacements: List[Replacement], errors: List[String]): Validation[String] =
      replacements match {
        case Nil => errors match {
          case head :: tail => Validated.invalid(NonEmptyChain(head, tail: _*))
          case Nil          => Validated.valid(line)
        }
        case replacement :: tail =>
          findPropertyValue(lineNumber, replacement.propertyName, map) match {
            case Validated.Valid(ValueSubstitution(value)) =>
              val nextLine = line.replace(replacement.replacementText, value)
              formatLineRec(nextLine, tail, errors)

            case Validated.Valid(wrongSubstitution) =>
              val substitutionType = wrongSubstitution.getClass.getSimpleName.stripSuffix("$")
              val errorMsg = s"(line $lineNumber): value substitution expected for '${replacement.propertyName}' but $substitutionType found"
              formatLineRec(line, tail, errors :+ errorMsg)

            case Validated.Invalid(err) => formatLineRec(line, tail, errors ++ err.iterator)
          }
      }

    formatLineRec(section.textLine.text, section.replacements, List.empty)
  }

  private final def formatForeachSection(section: ForeachSection, map: SubstitutionMap): ValidatedLines = {
    val startLineNumber = section.startLine.lineNumber
    val nestedSections = section.nestedSections

    val iteratorSubstitution = findPropertyValue(startLineNumber, section.iteratorName, map) match {
      case Validated.Valid(col@Collection(_)) => Right(col: Collection)
      case Validated.Invalid(errors)          => Left(errors)
    }

    @tailrec def formatForeachTemplateRec(
      elements: List[Substitution],
      lines: Seq[String],
      errors: Seq[String]
    ): Either[NonEmptyChain[String], Seq[String]] =
      elements match {
        case Nil => errors match {
          case first :: rest  => Left(NonEmptyChain(first, rest:_*))
          case Nil            => Right(lines)
        }

        case element :: tail =>
          val sectionSubMap: SubstitutionMap = map + (section.blockPropertyName -> element)
          val formattedSections = nestedSections.map(formatSection(_, sectionSubMap))
          val sectionResult = combineSectionResults(formattedSections)

          sectionResult.toEither match {
            case Right(sectionLines)  => formatForeachTemplateRec(tail, lines ++ sectionLines, errors)
            case Left(sectionErrors)  => formatForeachTemplateRec(tail, lines, errors ++ sectionErrors.iterator)
          }
      }

    val finalSectionResult = iteratorSubstitution flatMap (col =>
      formatForeachTemplateRec(col.elements.toList, List.empty, List.empty)
    )

    Validated.fromEither(finalSectionResult)
  }

  private final def findPropertyValue(lineNo: Int, name: String, map: SubstitutionMap): Validation[Substitution] = {
    @tailrec def findRec(tokens: List[String], target: Substitution): Validation[Substitution] =
      (tokens, target) match {
        case (Nil, valid) => Validated.valid(valid)
        case (head :: tail, PropertyMap(props)) if props.contains(head) => findRec(tail, props(head))
        case (_, _) =>
          val errorMsg = s"(line $lineNo): cannot find replacement '$name' in substitutions"
          Validated.invalid(NonEmptyChain.one(errorMsg))
      }

    val tokens = name.split('.').view.map(_.toLowerCase).toList
    val head :: tail = tokens

    map.get(head) match {
      case Some(target) => findRec(tail, target)
      case None => Validated.invalid(NonEmptyChain.one(s"(line $lineNo): cannot find replacement '$name' in substitutions"))
    }
  }

  private final def combineSectionResults(results: Iterable[ValidatedLines]): ValidatedLines = {
    val emptyLines: ValidatedLines = Validated.valid(Seq.empty[String])
    results.foldLeft(emptyLines) { case (acc, sectionLines) =>
      acc.combine(sectionLines)
    }
  }

}

private object TemplateSubstitutions {
  final type SubstitutionMap = Map[String, Substitution]

  def substitutionsMap(input: Map[String, Any]): SubstitutionMap =
    input map { case (key, value) => (key.toLowerCase, createSubstitution(value)) }

  private def createSubstitution(value: Any): Substitution = value match {
    case iter: Iterable[_] => createCollection(iter)
    case iter: Iterator[_] => createCollection(iter.to(Iterable))
    case prod: Product     => createPropertyMap(prod)
    case _                 => ValueSubstitution(value.toString)
  }

  private def createCollection(iter: Iterable[Any]): Collection = {
    Collection(iter.map(createSubstitution))
  }

  private def createPropertyMap(product: Product): PropertyMap = {
    val props = (0 until product.productArity).map { index =>
      val name = product.productElementName(index).toLowerCase
      val value = createSubstitution(product.productElement(index))
      (name, value)
    }.toMap

    PropertyMap(props)
  }

  sealed trait Substitution

  final case class ValueSubstitution(value: String) extends Substitution

  final case class Collection(elements: Iterable[Substitution]) extends Substitution

  final case class PropertyMap(props: Map[String, Substitution]) extends Substitution {
    def hasProperty(name: String): Boolean = {
      @tailrec def checkRec(tokens: List[String], target: PropertyMap): Boolean =
        tokens match {
          case Nil => true
          case last :: Nil => target.props.contains(last)
          case head :: tail => target.props.get(head) match {
            case None => false
            case Some(p@PropertyMap(_)) => checkRec(tail, p)
            case Some(_) => false
          }
        }

      checkRec(name.split('.').toList, this)
    }

    def getValue(name: String): String = {
      @tailrec def getRec(tokens: List[String], target: Substitution): String =
        (tokens, target) match {
          case (Nil, ValueSubstitution(value))    => value
          case (head :: tail, PropertyMap(props)) => getRec(tail, props(head))
          case (_, _)                             => throw new RuntimeException("Bad getValue")
        }

      getRec(name.split('.').toList, this)
    }
  }

}
