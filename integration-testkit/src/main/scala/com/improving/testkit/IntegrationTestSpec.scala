package com.improving.testkit

import org.scalatest._
import org.scalatest.verbs.ShouldVerb

import scala.concurrent.ExecutionContext

/**
 * Base class for all Kalix Integration Tests (use this for your specs!)
 *
 * @example Use like {{{class MyTestSpecs extends IntegrationTestSpec with AnyWordSpecLike with Matchers}}}
 *
 * @note You can use this base spec with any sync specs, like `AnyWordSpecLike` or `AnyFunSpecLike`.
 * @note You __cannot__ use this base spec with any of the async specs, like `AsyncWordSpecLike`!
 *
 * @see `com.improving.testkit.IntegrationTestSpecTest` in the `integration-testkit-tests` module for an example
 *      of how to use this base spec. It should be very easy to use and "just do the right thing" (tm)
 */
abstract class IntegrationTestSpec extends internal.IntegrationTestSpecImpl {
  this: TestSuite with ShouldVerb with Informing with Notifying =>

  /** You must implement this method in your specification! */
  protected def configureTestKit(builder: TestKitBuilder): IntegrationTestKit

  /** Only use the [[ExecutionContext executionContext]] provided by this class! */
  implicit protected final def executionContext: ExecutionContext =
    getTestKit match {
      case Some(testKit) => testKit.executionContext
      case None          => ExecutionContext.global
    }

  /**
   * This is the [[IntegrationTestKit TestKit]] available to your test specs.
 *
   * @note It is __not__ available during class construction!
   */
  protected final def testKit: IntegrationTestKit =
    getTestKit match {
      case Some(impl) => impl
      case None       => throw new IntegrationTestError("TestKit not yet built")
    }

}
