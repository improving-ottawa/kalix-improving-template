package com.improving.extensions.oidc

import cats.effect.IO
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.Span
import org.scalatest.wordspec.AnyWordSpecLike
import sttp.model.Uri

import scala.concurrent.Future
import scala.concurrent.duration._

class JWKClientSpec extends AnyWordSpecLike with Matchers with AsyncContextSpec {

  implicit val patience: PatienceConfig = PatienceConfig(500.millis, Span(100, org.scalatest.time.Millis))

  import context._

  /* A JwksUri which should usually be available */

  private val googleJwksUri = Uri.unsafeParse("https://www.googleapis.com/oauth2/v3/certs")

  /* Test Specifications */

  "JWKClient" when {

    "using Cats Effect context" should {

      val client: JWKClientImpl[IO] = JWKClient.catsEffect.asInstanceOf[JWKClientImpl[IO]]

      "be able to retrieve Json Web Keys (JWKs) from Uri" in {

        val result = client.retrieveJwks("clientId", googleJwksUri).attempt.unsafeRunSync()
        result match {
          case Right(value) => value.size must be > 0
          case Left(error)  => fail(error)
        }

      }

      "be able to retrieve JWK from internal client cache after it has been retrieved" in {
        val result    = client.retrieveFromCache("clientId").attempt.unsafeRunSync()
        if (result.isLeft) fail(result.left.get)
        val maybeJwks = result.getOrElse(None)

        maybeJwks match {
          case Some(value) => value.size must be > 0
          case None        => fail("Could not retrieve JWKs from client cache.")
        }
      }

    }

    "using Scala Future context" should {

      val client: JWKClientImpl[Future] =
        JWKClient.scalaFuture(context.blockingContext).asInstanceOf[JWKClientImpl[Future]]

      "be able to retrieve Json Web Keys (JWKs) from Uri" in {
        val result = client.retrieveJwks("clientId", googleJwksUri).futureValue
        result.size must be > 0
      }

      "be able to retrieve JWK from internal client cache after it has been retrieved" in {
        val result = client.retrieveFromCache("clientId").futureValue
        result match {
          case Some(value) => value.size must be > 0
          case None        => fail("Could not retrieve JWKs from client cache.")
        }
      }

    }

  }

}
