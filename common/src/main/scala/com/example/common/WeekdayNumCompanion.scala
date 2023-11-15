package com.example.common

import scalapb._

trait WeekdayNumCompanion extends GeneratedMessageCompanion[WeekdayNum] {
  def apply(weekday: Weekday): WeekdayNum = WeekdayNum(0, weekday)
}
