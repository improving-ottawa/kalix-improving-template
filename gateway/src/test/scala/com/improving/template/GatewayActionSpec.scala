package com.improving.template

import akka.actor.ActorSystem
import com.improving.template.domain.DoNothingTwiceCommand
import com.typesafe.config.ConfigFactory
import kalix.javasdk.impl.GrpcClients
import kalix.scalasdk.KalixRunner
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters.MapHasAsJava
import com.google.protobuf.empty.Empty
import org.scalatest.time.{Millis, Seconds, Span}

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class GatewayActionSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll with ScalaFutures with Eventually {

  implicit private val patience: PatienceConfig = {
    PatienceConfig(Span(30, Seconds), Span(500, Millis))
  }

  private val testSystem = ActorSystem("test-system")

  private val kalixRunner: KalixRunner = {
    val confMap = Map(
      // don't kill the test JVM when terminating the KalixRunner
      "kalix.system.akka.coordinated-shutdown.exit-jvm" -> "off",
      "kalix.dev-mode.docker-compose-file" -> "docker-compose.yml",
      "kalix.user-function-interface" -> "0.0.0.0"
    )

    val config = ConfigFactory.parseMap(confMap.asJava).withFallback(ConfigFactory.load())
    Main.createKalix().createRunner(config)
  }

  override def beforeAll(): Unit =
    kalixRunner.run()

  override def afterAll(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val allStopped =
      Future.sequence(Seq(testSystem.terminate(), kalixRunner.terminate()))
    Await.result(allStopped, 5.seconds)
  }

  val gatewayClient =
    GatewayActionTestKit(new GatewayAction(_))

  "GatewayAction" must {

    "handle command DoNothingTwice" in {
      eventually {
        gatewayClient.doNothingTwice(DoNothingTwiceCommand()) shouldBe Empty.defaultInstance
      }
    }

  }
}
