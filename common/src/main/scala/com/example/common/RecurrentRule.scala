package com.example.common

import cats.data.NonEmptySeq
import scalapb._
import scalapb.validate.ValidationException

import scala.annotation.tailrec
import java.time._

sealed trait RecurrentRulePart {
  def partName: String
  def toString: String
}

object RecurrentRulePart {

  sealed abstract class RuleBase(val partName: String, elements: => Iterator[Any]) extends RecurrentRulePart {

    override def toString: String = {
      val elementsText = elements.toSeq.map(_.toString) match {
        case Seq(single) => single
        case multiple    => multiple.mkString(",")
      }
      s"$partName=$elementsText"
    }

  }

  final private def formatWeekdayNum(wdn: WeekdayNum): String =
    if (wdn.pos == 0) wdn.weekday.name
    else s"${wdn.pos}${wdn.weekday.name}"

  case class BySetPosition(positions: NonEmptySeq[Int])       extends RuleBase("BYSETPOS", positions.iterator)
  case class ByMonth(months: NonEmptySeq[Month])              extends RuleBase("BYMONTH", months.iterator.map(_.getValue))
  case class ByDayOfMonth(daysOfMonth: NonEmptySeq[Int])      extends RuleBase("BYMONTHDAY", daysOfMonth.iterator)
  case class ByDayOfYear(daysOfYear: NonEmptySeq[Int])        extends RuleBase("BYYEARDAY", daysOfYear.iterator)
  case class ByWeekOfYear(weeksOfYear: NonEmptySeq[Int])      extends RuleBase("BYWEEKNO", weeksOfYear.iterator)
  case class ByDayOfWeek(daysOfWeek: NonEmptySeq[WeekdayNum]) extends RuleBase("BYDAY", daysOfWeek.iterator.map(formatWeekdayNum))

}

sealed trait RecurrentRule {
  import RecurrentRule._

  /** All formatting parts of this [[RecurrentRule rrule]]. */
  final private def formattingParts: Seq[String] = {
    val startParts = Seq(
      s"FREQ=$frequency",
      s"INTERVAL=$interval"
    )

    val optionalParts = Seq(
      untilCount.map(n => s"COUNT=$n"),
      untilDate.map(dt => s"UNTIL=${dt.format(dateFormatter)}"),
      startDayOfWeek.map(dow => s"WKST=${dow.name.take(2)}")
    ).collect { case Some(partStr) => partStr }

    startParts ++ optionalParts ++ ruleParts.map(_.toString).iterator
  }

  /** ICal / formatted string representation of this [[RecurrentRule rrule]] */
  final def formatted: String = {
    val dtStart = startDate.map(dt => s"DTSTART:${formatISO8601(dt)}\n").getOrElse("")
    val exDate  = exclusions match {
      case Nil   => ""
      case elems => s"\nEXDATE:${elems.map(formatISO8601).mkString(",")}"
    }

    s"${dtStart}RRULE:${formattingParts.mkString(";")}$exDate"
  }

  /** String representation of this [[RecurrentRule rrule]]. */
  override def toString: String = {
    val dtStart = startDate.map(dt => s"DTSTART=${formatISO8601(dt)}")
    val exDates = exclusions match {
      case Nil  => None
      case elms => Some(s"EXDATE=${elms.map(formatISO8601).mkString("[", ", ", "]")}")
    }

    val allParts = (dtStart.toSeq ++ formattingParts ++ exDates.toSeq).mkString("; ")
    s"RRule($allParts)"
  }

  final override def hashCode(): Int = {
    import scala.util.hashing.MurmurHash3
    MurmurHash3.unorderedHash(
      Array(
        frequency,
        interval,
        untilCount,
        untilDate,
        startDayOfWeek,
        exclusions,
        ruleParts.view.sortBy(_.partName)
      ),
      "RecurrentRule".##
    )
  }

  final override def equals(obj: Any): Boolean = obj match {
    case rr: RecurrentRule => equals(rr)
    case _                 => false
  }

  final def equals(x: RecurrentRule): Boolean =
    frequency == x.frequency &&
    interval == x.interval &&
    untilCount == x.untilCount &&
    untilDate == x.untilDate &&
    startDayOfWeek == x.startDayOfWeek &&
    exclusions == x.exclusions &&
    ruleParts.sortBy(_.partName) == x.ruleParts.sortBy(_.partName)

  def frequency: Frequency
  def interval: Int

  def untilCount: Option[Int]
  def untilDate: Option[LocalDate]

  def startDate: Option[LocalDate]
  def startDayOfWeek: Option[DayOfWeek]

  /** Any excluded dates for this rule. */
  def exclusions: Seq[LocalDate]

  /** All of the specified [[RecurrentRulePart rrule-parts]] in this recurrent rule. */
  def ruleParts: List[RecurrentRulePart]

  /**
    * A series of all [[LocalDate instances]] defined by this rule.
    * @note
    *   This series __can__ be (effectively) infinite.
    */
  def series: LazyList[LocalDate]

  /** Gets the next [[LocalDate instance]] occurring on or after `fromDate`. */
  def nextInstance(fromDate: LocalDate): Option[LocalDate]

  /** Creates a new `series` using `fromDate` as the starting point. */
  def createSeries(fromDate: LocalDate): LazyList[LocalDate]
}

sealed trait BuildableRule extends RecurrentRule {

  /** Finalize this [[BuildableRule buildable rrule]] so that further modifications cannot be made. */
  final def sealRule: RecurrentRule = this

  def withStartDate(dt: LocalDate): BuildableRule
  def withWeekStart(dayOfWeek: DayOfWeek): BuildableRule
  def withInterval(i: Int): BuildableRule
  def withCount(count: Int): BuildableRule
  def withUntil(dt: LocalDate): BuildableRule

  def bySetPosition(first: Int, rest: Int*): BuildableRule
  def byMonth(first: Int, rest: Int*): BuildableRule

  final def byMonth(first: Month, rest: Month*): BuildableRule =
    byMonth(first.getValue, rest.map(_.getValue): _*)

  def byMonthDay(first: Int, rest: Int*): BuildableRule
  def byYearDay(first: Int, rest: Int*): BuildableRule
  def byWeekNumber(first: Int, rest: Int*): BuildableRule
  def byWeekday(first: WeekdayNum, rest: WeekdayNum*): BuildableRule

  def withExcludedDates(first: LocalDate, rest: LocalDate*): BuildableRule
}

sealed trait ProtobufRRuleConversion { self: RecurrentRule.type =>
  import com.google.`type`.date.{Date => ProtoDate}
  import com.google.`type`.month.{Month => ProtoMonth}

  trait RRuleCompanion extends GeneratedMessageCompanion[protobuf.RRule] {

    implicit final val rruleScalaTypeMapper: TypeMapper[protobuf.RRule, RecurrentRule] =
      self.recurrentRuleTypeMapper

  }

  final val recurrentRuleTypeMapper: TypeMapper[protobuf.RRule, RecurrentRule] =
    TypeMapper(protoToScala)(scalaToProto)

  private val protoDateMapper =
    TypeMapper[ProtoDate, LocalDate](proto => LocalDate.of(proto.year, proto.month, proto.day))(ldt =>
      ProtoDate(ldt.getYear, ldt.getMonthValue, ldt.getDayOfMonth)
    )

  private val protoMonthMapper =
    TypeMapper[ProtoMonth, Month](proto => Month.of(proto.value))(mon => ProtoMonth.fromValue(mon.getValue))

  final private def protoWeekdayToDayOfWeek(wd: Weekday): DayOfWeek =
    DayOfWeek.of(wd.value + 1)

  final private def dayOfWeekToProtoWeekday(dow: DayOfWeek): Weekday =
    Weekday.fromValue(dow.getValue - 1)

  final private def protoMonthOfYearToMonth(moy: protobuf.MonthOfYear): Month =
    moy.value match {
      case protobuf.MonthOfYear.Value.Month(value)     => protoMonthMapper.toCustom(value)
      case protobuf.MonthOfYear.Value.MonthNumber(num) => Month.of(num)
      case protobuf.MonthOfYear.Value.Empty            => throw new ValidationException("MonthOfYear value not specified")
    }

  final private def monthToProtoMonthOfYear(month: Month): protobuf.MonthOfYear =
    protobuf.MonthOfYear(protobuf.MonthOfYear.Value.Month(protoMonthMapper.toBase(month)))

  final private def scalaToProto(rrule: RecurrentRule): protobuf.RRule = {
    val baseResult = protobuf.RRule(
      rrule.frequency,
      rrule.interval,
      rrule.untilCount,
      rrule.untilDate.map(protoDateMapper.toBase),
      rrule.startDate.map(protoDateMapper.toBase),
      rrule.startDayOfWeek.map(dayOfWeekToProtoWeekday)
    )

    @tailrec def processParts(remParts: List[RecurrentRulePart], instance: protobuf.RRule): protobuf.RRule = {
      import RecurrentRulePart._
      remParts match {
        case Nil                        => instance
        case BySetPosition(pos) :: tail => processParts(tail, instance.withByPosition(pos.toSeq))
        case ByMonth(mons) :: tail      =>
          processParts(tail, instance.withByMonth(mons.iterator.map(monthToProtoMonthOfYear).toSeq))
        case ByDayOfMonth(doms) :: tail => processParts(tail, instance.withByMonthDay(doms.toSeq))
        case ByDayOfYear(doys) :: tail  => processParts(tail, instance.withByYearDay(doys.toSeq))
        case ByWeekOfYear(woys) :: tail => processParts(tail, instance.withByWeekNumber(woys.toSeq))
        case ByDayOfWeek(dows) :: tail  => processParts(tail, instance.withByWeekday(dows.toSeq))
      }
    }

    processParts(rrule.ruleParts, baseResult)
  }

  final private def protoToScala(rrule: protobuf.RRule): RecurrentRule = {
    import RecurrentRulePart._

    val ruleParts = Seq(
      NonEmptySeq.fromSeq(rrule.byPosition).map(BySetPosition),
      NonEmptySeq.fromSeq(rrule.byMonth.map(protoMonthOfYearToMonth)).map(ByMonth),
      NonEmptySeq.fromSeq(rrule.byMonthDay).map(ByDayOfMonth),
      NonEmptySeq.fromSeq(rrule.byYearDay).map(ByDayOfYear),
      NonEmptySeq.fromSeq(rrule.byWeekNumber).map(ByWeekOfYear),
      NonEmptySeq.fromSeq(rrule.byWeekday).map(ByDayOfWeek)
    ).collect { case Some(rrulePart) => rrulePart: RecurrentRulePart }

    if (rrule.frequency == Frequency.FrequencyNever) RecurrentRule.never
    else {
      createInstance(
        rrule.frequency,
        rrule.interval,
        rrule.count,
        rrule.until.map(protoDateMapper.toCustom),
        rrule.startDate.map(protoDateMapper.toCustom),
        rrule.weekStart.map(protoWeekdayToDayOfWeek),
        rrule.exclusions.map(protoDateMapper.toCustom),
        ruleParts
      )
    }
  }

}

object RecurrentRule extends ProtobufRRuleConversion {
  def never: RecurrentRule = NeverRuleInstance

  def apply(frequency: Frequency): BuildableRule = apply(frequency, 1)

  def apply(frequency: Frequency, interval: Int): BuildableRule = {
    assert(frequency != Frequency.FrequencyNever, "Frequency cannot be `NEVER`, use `RecurrentRule.never` instead.")
    BuildableInstance(frequency, interval)
  }

  private val utcZoneId     = ZoneId.of("UTC")
  private val dateFormatter = format.DateTimeFormatter.ofPattern("yyyyMMdd")

  final private def formatISO8601(dt: LocalDate): String =
    dt.atStartOfDay(utcZoneId).format(format.DateTimeFormatter.ISO_ZONED_DATE_TIME)

  final protected def createInstance(
    frequency: Frequency,
    interval: Int,
    untilCount: Option[Int],
    untilDate: Option[LocalDate],
    startDate: Option[LocalDate],
    startDayOfWeek: Option[DayOfWeek],
    exclusions: Seq[LocalDate],
    ruleParts: Seq[RecurrentRulePart]
  ): RecurrentRule =
    Instance(
      frequency,
      interval,
      untilCount,
      untilDate,
      startDate,
      startDayOfWeek,
      ruleParts = ruleParts.toList,
      exclusions = exclusions
    )

  final private case class Instance(
    frequency: Frequency,
    interval: Int,
    untilCount: Option[Int],
    untilDate: Option[LocalDate],
    startDate: Option[LocalDate],
    startDayOfWeek: Option[DayOfWeek],
    ruleParts: List[RecurrentRulePart],
    exclusions: Seq[LocalDate]
  ) extends RecurrenceRuleImpl(
        frequency,
        interval,
        untilCount,
        untilDate,
        startDate,
        startDayOfWeek,
        exclusions
      )
      with RecurrentRule

  import RecurrentRulePart._

  final private case class BuildableInstance(
    frequency: Frequency,
    interval: Int,
    untilCount: Option[Int] = None,
    untilDate: Option[LocalDate] = None,
    startDate: Option[LocalDate] = None,
    startDayOfWeek: Option[DayOfWeek] = None,
    exclusions: Seq[LocalDate] = Seq.empty,
    bySetPosition: Option[BySetPosition] = None,
    byMonth: Option[ByMonth] = None,
    byDayOfMonth: Option[ByDayOfMonth] = None,
    byDayOfYear: Option[ByDayOfYear] = None,
    byWeekOfYear: Option[ByWeekOfYear] = None,
    byDayOfWeek: Option[ByDayOfWeek] = None
  ) extends RecurrenceRuleImpl(
        frequency,
        interval,
        untilCount,
        untilDate,
        startDate,
        startDayOfWeek,
        exclusions
      )
      with BuildableRule {
    def withStartDate(dt: LocalDate): BuildableRule        = copy(startDate = Some(dt))
    def withWeekStart(dayOfWeek: DayOfWeek): BuildableRule = copy(startDayOfWeek = Some(dayOfWeek))
    def withInterval(i: Int): BuildableRule                = copy(interval = i)
    def withCount(count: Int): BuildableRule               = copy(untilCount = Some(count))
    def withUntil(dt: LocalDate): BuildableRule            = copy(untilDate = Some(dt))

    @inline private def nonEmpty(first: Int, rest: Seq[Int]): NonEmptySeq[Int] =
      NonEmptySeq(first, rest)

    def bySetPosition(first: Int, rest: Int*): BuildableRule =
      copy(bySetPosition = Some(BySetPosition(nonEmpty(first, rest))))

    def byMonth(first: Int, rest: Int*): BuildableRule =
      copy(byMonth = Some(ByMonth(nonEmpty(first, rest).map(Month.of))))

    def byMonthDay(first: Int, rest: Int*): BuildableRule =
      copy(byDayOfMonth = Some(ByDayOfMonth(nonEmpty(first, rest))))

    def byYearDay(first: Int, rest: Int*): BuildableRule = copy(byDayOfYear = Some(ByDayOfYear(nonEmpty(first, rest))))

    def byWeekNumber(first: Int, rest: Int*): BuildableRule =
      copy(byWeekOfYear = Some(ByWeekOfYear(nonEmpty(first, rest))))

    def byWeekday(first: WeekdayNum, rest: WeekdayNum*): BuildableRule =
      copy(byDayOfWeek = Some(ByDayOfWeek(NonEmptySeq(first, rest))))

    def withExcludedDates(first: LocalDate, rest: LocalDate*): BuildableRule = copy(exclusions = first +: rest)

    lazy val ruleParts: List[RecurrentRulePart] =
      List(
        bySetPosition,
        byMonth,
        byDayOfMonth,
        byDayOfYear,
        byWeekOfYear,
        byDayOfWeek
      ).collect { case Some(rulePart) => rulePart }

  }

  private object NeverRuleInstance extends RecurrentRule {
    val frequency: Frequency = Frequency.FrequencyNever
    val interval: Int        = 0

    val untilCount: Option[Int]      = None
    val untilDate: Option[LocalDate] = None

    val startDate: Option[LocalDate]      = None
    val startDayOfWeek: Option[DayOfWeek] = None

    val exclusions: Seq[LocalDate]         = Seq.empty
    val ruleParts: List[RecurrentRulePart] = List.empty
    val series: LazyList[LocalDate]        = LazyList.empty

    def nextInstance(fromDate: LocalDate): Option[LocalDate]   = None
    def createSeries(fromDate: LocalDate): LazyList[LocalDate] = LazyList.empty

    override def toString: String = "RRule(NEVER)"
  }

}
