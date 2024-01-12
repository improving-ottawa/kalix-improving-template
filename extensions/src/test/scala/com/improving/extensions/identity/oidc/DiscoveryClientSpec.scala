package com.improving.extensions.identity.oidc

import org.scalatest.matchers.must.Matchers
import org.scalatest.time.Span
import org.scalatest.wordspec.AnyWordSpecLike
import sttp.model.Uri

import scala.concurrent.duration._

class DiscoveryClientSpec extends AnyWordSpecLike with Matchers with AsyncContextSpec {
  import context._

  implicit val patience: PatienceConfig = PatienceConfig(500.millis, Span(100, org.scalatest.time.Millis))

  /* A OIDC discovery endpoint which should usually be available */
  private val discoveryUri = Uri.unsafeParse("https://accounts.google.com/.well-known/openid-configuration")

  /* Test Specifications */

  "DiscoveryClient" when {

    "using Cats Effect context" should {

      "be able to retrieve and parse an OIDC discovery response from a valid discovery endpoint" in {
        val client = DiscoveryClient.catsEffect
        val result = client.retrieveMetadata(discoveryUri).unsafeRunSync()
        result.claimsSupported.isEmpty mustBe false
      }

    }

    "using Scala Future context" should {

      "be able to retrieve and parse an OIDC discovery response from a valid discovery endpoint" in {
        val client = DiscoveryClient.scalaFuture(context.blockingContext)
        val result = client.retrieveMetadata(discoveryUri).futureValue
        result.claimsSupported.isEmpty mustBe false
      }

    }

  }

}
