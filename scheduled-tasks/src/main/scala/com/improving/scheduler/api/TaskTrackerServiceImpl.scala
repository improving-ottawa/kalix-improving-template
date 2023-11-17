package com.improving.scheduler.api

import com.improving.scheduler.domain.TaskStatus
import kalix.scalasdk.action.{Action, ActionCreationContext}

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class TaskTrackerServiceImpl(creationContext: ActionCreationContext) extends AbstractTaskTrackerServiceImpl {

  override def getTaskStatus(request: TaskStatusRequest): Action.Effect[TaskStatus] =
    effects.forward(components.taskTrackerEntity.getTaskStatus(request))

  override def getLastTaskRunResult(req: TaskResultRequest): Action.Effect[TaskResultResponse] =
    effects.forward(components.taskTrackerEntity.getLastRunResult(TaskStatusRequest(req.taskId)))

}
