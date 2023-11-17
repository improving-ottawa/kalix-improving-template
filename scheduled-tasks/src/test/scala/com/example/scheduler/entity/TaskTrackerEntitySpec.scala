package com.example.scheduler.entity

import com.google.protobuf.empty.Empty
import com.example.scheduler.api.TaskStatusRequest
import com.example.scheduler.domain.TaskStatus
import com.example.scheduler.entity
import kalix.scalasdk.testkit.ValueEntityResult
import kalix.scalasdk.valueentity.ValueEntity
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TaskTrackerEntitySpec
    extends AnyWordSpec
    with Matchers {

  "TaskTrackerEntity" must {

    "have example test that can be removed" in {
      val service = TaskTrackerEntityTestKit(new TaskTrackerEntity(_))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // val reply = result.getReply()
      // reply shouldBe expectedReply
      // verify the final state after the command
      // service.currentState() shouldBe expectedState
    }

    "handle command GetTaskStatus" in {
      val service = TaskTrackerEntityTestKit(new TaskTrackerEntity(_))
      pending
      // val result = service.getTaskStatus(TaskStatusRequest(...))
    }

    "handle command MarkTaskScheduled" in {
      val service = TaskTrackerEntityTestKit(new TaskTrackerEntity(_))
      pending
      // val result = service.markTaskScheduled(TaskCommand(...))
    }

    "handle command MarkTaskRunning" in {
      val service = TaskTrackerEntityTestKit(new TaskTrackerEntity(_))
      pending
      // val result = service.markTaskRunning(TaskCommand(...))
    }

    "handle command MarkTaskCompleted" in {
      val service = TaskTrackerEntityTestKit(new TaskTrackerEntity(_))
      pending
      // val result = service.markTaskCompleted(TaskCompletedCommand(...))
    }

  }
}
