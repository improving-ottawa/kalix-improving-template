package com.example.boundedContext

import com.example.boundedContext.api._
import com.example.boundedContext.entity._
import com.example.service3.api.{NoData3Service, NoData3ServiceProvider}
import com.example.service3.entity.{Service3Entity, Service3EntityProvider}
import kalix.scalasdk.Kalix
import org.slf4j.LoggerFactory

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

object Main {

  private val log = LoggerFactory.getLogger("com.example.boundedContext.Main")

  def createKalix(): Kalix = {
    // The KalixFactory automatically registers any generated Actions, Views or Entities,
    // and is kept up-to-date with any changes in your protobuf definitions.
    // If you prefer, you may remove this and manually register these components in a
    // `Kalix()` instance.
    KalixFactory
      .withComponents(
        new Service1Entity(_),
        new PingPong(_),
        new Service1Impl(_),
        new Service2Impl(_)
      )
      .register(Service3EntityProvider(new Service3Entity(_)))
      .register(NoData3ServiceProvider(new NoData3Service(_)))

  }

  def main(args: Array[String]): Unit = {
    log.info("starting the Kalix service")
    createKalix().start()
  }

}
