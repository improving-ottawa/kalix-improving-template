package com.improving.scheduler.entity

import com.improving.scheduler.api._
import com.improving.scheduler.domain._
import com.google.protobuf.empty.Empty
import com.improving.scheduler.entity
import kalix.scalasdk.valueentity.{ValueEntity, ValueEntityContext}

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class TaskTrackerEntity(context: ValueEntityContext) extends AbstractTaskTrackerEntity {

  override def emptyState: TaskTrackerState =
    entity.TaskTrackerState(
      taskId = context.entityId,
      state = TaskState.TASK_STATE_NOT_SCHEDULED,
      lastResult = None
    )

  override def getTaskStatus(
    currentState: TaskTrackerState,
    request: TaskStatusRequest
  ): ValueEntity.Effect[TaskStatus] =
    effects.reply(
      TaskStatus(
        taskId = currentState.taskId,
        state = currentState.state
      )
    )

  override def getLastRunResult(
    currentState: TaskTrackerState,
    request: TaskStatusRequest
  ): ValueEntity.Effect[TaskResultResponse] =
    effects.reply(
      TaskResultResponse(
        taskId = currentState.taskId,
        result = currentState.lastResult
      )
    )

  override def markTaskScheduled(currentState: TaskTrackerState, command: TaskCommand): ValueEntity.Effect[Empty] =
    if (currentState.state != TaskState.TASK_STATE_NOT_SCHEDULED) {
      val updated = currentState.copy(state = TaskState.TASK_STATE_SCHEDULED)
      effects.updateState(updated).thenReply(Empty.of())
    } else effects.reply(Empty.of())

  override def markTaskRunning(
    currentState: TaskTrackerState,
    command: TaskCommand
  ): ValueEntity.Effect[TaskStartResponse] =
    if (currentState.state == TaskState.TASK_STATE_RUNNING) effects.reply(TaskStartResponse(canStart = false))
    else {
      val updated = currentState.copy(state = TaskState.TASK_STATE_RUNNING)
      effects.updateState(updated).thenReply(TaskStartResponse(canStart = true))
    }

  override def markTaskCompleted(
    currentState: TaskTrackerState,
    command: TaskCompletedCommand
  ): ValueEntity.Effect[Empty] = {
    val updated = currentState.copy(state = TaskState.TASK_STATE_NOT_SCHEDULED, lastResult = Some(command.result))
    effects
      .updateState(updated)
      .thenReply(Empty.of())
  }

}
