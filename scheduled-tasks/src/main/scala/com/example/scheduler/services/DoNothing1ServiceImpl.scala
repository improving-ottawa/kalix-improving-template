package com.example.scheduler.services

import akka.NotUsed
import akka.stream.scaladsl._
import com.example.boundedContext.api.NoData1
import com.example.boundedContext.domain._
import com.example.boundedContext.entity.Service1
import com.google.protobuf.empty.Empty
import kalix.scalasdk.DeferredCall
import kalix.scalasdk.action.ActionCreationContext

import scala.concurrent.Future
import java.time._

// Note: Business logic for computing company match is implemented here
object DoNothing1 {
  final val taskKey: String = "DoNothing1"
}

class DoNothing1ServiceImpl(protected val creationContext: ActionCreationContext)
    extends AbstractDoNothing1ServiceImpl
    with ScheduledTaskAction
    with DailyTaskSchedule {
  import DoNothing1._

  val taskKey: String         = DoNothing1.taskKey
  val dailyRunTime: LocalTime = LocalTime.of(2, 0, 0)

  // Various gRPC clients
  private lazy val service1Client = grpcClients.getGrpcClient(classOf[Service1], "bounded-context")
  private lazy val noData1Client  = grpcClients.getGrpcClient(classOf[NoData1], "bounded-context")

  protected def runTaskDeferredCall: DeferredCall[Empty, Empty] =
    components.doNothing1ServiceImpl.run(Empty.of())

  protected def taskAction(nowInstant: Instant): Future[Unit] = {
    val postProcessFlow = postProcessSingleDoNothing1()

    processSingleNothing()
      .via(postProcessFlow)
      .runWith(Sink.ignore)
      .map(_ => ())
  }

  private def processSingleNothing(): Source[Unit, NotUsed] = {
    noData1Client.getNothing(Empty.defaultInstance).map(_ => ())
  }

  private def postProcessSingleDoNothing1() = Flow[Unit]
    .mapAsync(1) { _ =>
      for {
        _ <- service1Client.doNothing(DoNothingCommand1.defaultInstance)
      } yield ()
    }

}
