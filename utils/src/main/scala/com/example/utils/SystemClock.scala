package com.example.utils

import java.time._

object SystemClock {
  private val clock = Clock.systemUTC()

  def currentInstant: Instant = Instant.now(clock)

  def currentDateTime: LocalDateTime = LocalDateTime.now(clock)

  def currentDate: LocalDate = LocalDate.now(clock)

  def currentTime: LocalTime = LocalTime.now(clock)
}
