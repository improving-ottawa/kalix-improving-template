package com.example.utils.iam.jwt

import akka.http.scaladsl.model.DateTime
import com.google.protobuf.timestamp.Timestamp
import io.circe._
import io.circe.syntax._

import java.time.Instant

trait ClaimCodecHelpers {

  protected def writeSeconds(ts: Instant): Json = (ts.toEpochMilli / 1000).asJson

  protected def readSeconds(c: HCursor)(fieldName: String) =
    c.get[Long](fieldName).map(secs => Instant.ofEpochSecond(secs))

  protected def readSecondsOption(c: HCursor)(fieldName: String) =
    c.get[Option[Long]](fieldName).map { secsOpt =>
      secsOpt.map(secs => Instant.ofEpochSecond(secs))
    }

}
