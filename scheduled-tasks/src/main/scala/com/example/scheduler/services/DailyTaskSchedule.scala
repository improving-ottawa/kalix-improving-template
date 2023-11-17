package com.example.scheduler.services

import com.example.utils.SystemClock

import scala.concurrent.duration.FiniteDuration
import java.time._

trait DailyTaskSchedule { self: ScheduledTaskAction =>
  val dailyRunTime: LocalTime

  protected final def calculateDelayUntilNextRunTime(now: Instant): FiniteDuration = {
    val nextRunDateTime = SystemClock.currentDate.atTime(dailyRunTime)
    val nextRunInstant = nextRunDateTime.toInstant(ZoneOffset.UTC)
    val duration =
      if (now.isBefore(nextRunInstant)) Duration.between(now, nextRunInstant)
      else Duration.between(now, nextRunDateTime.plusDays(1).toInstant(ZoneOffset.UTC))

    FiniteDuration(duration.toSeconds, "seconds")
  }

}