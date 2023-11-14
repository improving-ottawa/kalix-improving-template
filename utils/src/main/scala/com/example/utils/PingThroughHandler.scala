package com.example.utils

/** Shared handler for [[PingThroughRequest]] handling. */
object PingThroughHandler {

  /** Creates a [[PingThroughResponse]], given a [[PingThroughRequest request]] and a `serviceName`. */
  def response(request: PingThroughRequest, serviceName: String): PingThroughResponse = {
    val processTimestamp = SystemClock.currentInstant
    val trace = ServiceTrace(serviceName, processTimestamp)

    PingThroughResponse(
      correlationId = request.correlationId,
      startTime = request.startTime,
      endTime = processTimestamp,
      traces = trace :: request.traces
    )
  }

}
