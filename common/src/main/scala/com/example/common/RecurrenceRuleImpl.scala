package com.example.common

import org.dmfs.rfc5545.recur.{
  Freq => JavaRRuleFreq,
  RecurrenceRule => JavaRRule,
  RecurrenceRuleIterator => JavaRRuleIterator
}

import org.dmfs.rfc5545.{
  DateTime => RRDateTime,
  Weekday => RRWeekday
}

import java.time._

abstract class RecurrenceRuleImpl private[common](
  frequency: Frequency,
  interval: Int,
  untilCount: Option[Int],
  untilDate: Option[LocalDate],
  startDate: Option[LocalDate],
  startDayOfWeek: Option[DayOfWeek],
  exclusions: Seq[LocalDate]
) {
  import RecurrenceRuleImpl._

  private[this] final lazy val cachedRRule: JavaRRule = createJavaRRule()

  final lazy val series: LazyList[LocalDate] =
    createSeries(cachedRRule, startDate.getOrElse(LocalDate.now(Clock.systemUTC)))

  final def nextInstance(fromDate: LocalDate): Option[LocalDate] =
    series.dropWhile(_.isBefore(fromDate)).headOption

  final def createSeries(fromDate: LocalDate): LazyList[LocalDate] = createSeries(cachedRRule, fromDate)

  def ruleParts: List[RecurrentRulePart]

  private[this] final def createJavaRRule() = {
    val rrule = new JavaRRule(frequencyToJava(frequency))
    rrule.setInterval(interval)

    untilCount.foreach(rrule.setCount)
    untilDate.foreach(dt => rrule.setUntil(localDateToRRDateTime(dt)))
    startDayOfWeek.foreach(dow => rrule.setWeekStart(dayOfWeekToWeekday(dow)))

    import scala.jdk.CollectionConverters._

    @inline def toJavaList(iter: Iterator[Int]): java.util.List[Integer] = {
      val outList = new java.util.ArrayList[Integer]()
      iter.map(Int.box).asJava.forEachRemaining { i => outList.add(i); () }
      outList
    }

    import RecurrentRulePart._
    for (elem <- ruleParts.iterator) elem match {
      case BySetPosition(pos) => rrule.setByPart(JavaRRule.Part.BYSETPOS, toJavaList(pos.iterator))
      case ByMonth(months)    => rrule.setByPart(JavaRRule.Part.BYMONTH, toJavaList(months.iterator.map(_.getValue)))
      case ByDayOfMonth(dom)  => rrule.setByPart(JavaRRule.Part.BYMONTHDAY, toJavaList(dom.iterator))
      case ByDayOfYear(doy)   => rrule.setByPart(JavaRRule.Part.BYYEARDAY, toJavaList(doy.iterator))
      case ByWeekOfYear(woy)  => rrule.setByPart(JavaRRule.Part.BYWEEKNO, toJavaList(woy.iterator))
      case ByDayOfWeek(dow)   => rrule.setByDayPart(java.util.Arrays.asList(dow.iterator.map(weekdayNumToRRWeekdayNum).toSeq: _*))
    }

    rrule
  }

  private[this] final def createSeries(rrule: JavaRRule, fromDate: LocalDate): LazyList[LocalDate] = {
    val exceptDates = exclusions.toSet
    val rruleIterator = rrule.iterator(localDateToRRDateTime(fromDate))

    // Return the series
    if (exceptDates.isEmpty)
      LazyList.from(convertIterator(rruleIterator)).dropWhile(_.compareTo(fromDate) <= 0)
    else
      LazyList.from(convertIterator(rruleIterator))
        .filterNot(exceptDates.contains)
        .dropWhile(_.compareTo(fromDate) <= 0)
  }

}

private object RecurrenceRuleImpl {
  private final val utcZoneId = ZoneId.of("UTC")

  @inline private final def frequencyToJava(frequency: Frequency): JavaRRuleFreq =
    JavaRRuleFreq.valueOf(frequency.name)

  @inline private final def dayOfWeekToWeekday(dow: DayOfWeek): RRWeekday =
    RRWeekday.valueOf(dow.name.take(2))

  @inline private final def weekdayNumToRRWeekdayNum(wdn: WeekdayNum): JavaRRule.WeekdayNum =
    new JavaRRule.WeekdayNum(wdn.pos, RRWeekday.valueOf(wdn.weekday.name))

  @inline private final def localDateToRRDateTime(ld: LocalDate): RRDateTime =
    new RRDateTime(
      RRDateTime.UTC,
      ld.atStartOfDay(utcZoneId).toInstant.toEpochMilli
    )

  private final def convertIterator(rrIterator: JavaRRuleIterator): Iterator[LocalDate] =
    new Iterator[LocalDate] {
      final def hasNext: Boolean = rrIterator.hasNext
      final def next(): LocalDate = {
        val nextInstance = Instant.ofEpochMilli(rrIterator.nextMillis())
        LocalDate.ofInstant(nextInstance, utcZoneId)
      }
    }

}
