package com.improving.extensions.oidc

import com.improving.utils.AsyncContext
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

trait AsyncContextSpec extends SuiteMixin with BeforeAndAfterAll with ScalaFutures { self: Suite =>
  final protected lazy val context = AsyncContext.catsEffect()

  override def afterAll(): Unit = {
    super.afterAll()
    context.shutdown()
  }

}
