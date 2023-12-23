package com.example.boundedContext

import com.example.boundedContext.api._
import com.example.boundedContext.entity._

import com.example.service3.{Main => Service3}
import kalix.scalasdk._
import org.slf4j.LoggerFactory

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

object Main {

  private val log = LoggerFactory.getLogger("com.example.boundedContext.Main")

  def createKalix(): Kalix = {
    val service3Builder = Service3.kalixBuilder

    val boundedContextBuilder = KalixBuilder.emptyBuilder
      .registerProvider(Service1EntityProvider(new Service1Entity(_)))
      .registerView(new NoData1View(_), NoData1ViewProvider.apply)
      .registerProvider(PingPongProvider(new PingPong(_)))
      .registerProvider(Service1ImplProvider(new Service1Impl(_)))
      .registerProvider(Service2ImplProvider(new Service2Impl(_)))
      .mergeWith(service3Builder)

    boundedContextBuilder.build
  }

  def main(args: Array[String]): Unit = {
    log.info("Starting the Kalix service")
    createKalix().start()
  }

}
