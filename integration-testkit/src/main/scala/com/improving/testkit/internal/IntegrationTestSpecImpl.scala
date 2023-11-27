package com.improving.testkit.internal

import com.improving.testkit._

import org.scalatest._

import java.lang.annotation.AnnotationFormatError
import java.nio.charset.CoderMalfunctionError
import javax.xml.parsers.FactoryConfigurationError
import javax.xml.transform.TransformerFactoryConfigurationError

abstract private[testkit] class IntegrationTestSpecImpl extends TestSuite { self: IntegrationTestSpec =>
  import IntegrationTestSpecImpl.anExceptionThatShouldCauseAnAbort

  @volatile private[this] var _testKitImpl: Option[TestKitImpl] = None

  private[testkit] def getTestKit: Option[IntegrationTestKit] = _testKitImpl

  final private def registerKit(): Unit = {
    val testKit = configureTestKit(TestKitImpl.emptyBuilder)
    _testKitImpl = Some(testKit.asInstanceOf[TestKitImpl])
  }

  override def run(testName: Option[String], args: Args): Status = {
    val (runStatus, thrownException) =
      try {
        if (!args.runTestInNewInstance && (expectedTestCount(args.filter) > 0))
          registerKit()
        (super.run(testName, args), None)
      } catch {
        case e: Exception => (FailedStatus, Some(e))
      }

    try {
      val statusToReturn =
        if (!args.runTestInNewInstance && (expectedTestCount(args.filter) > 0)) {
          // runStatus may not be completed, call afterAll only after it is completed
          runStatus.withAfterEffect {
            try _testKitImpl.foreach(_.stop())
            catch {
              case laterException: Exception
                  if !anExceptionThatShouldCauseAnAbort(laterException) && thrownException.isDefined =>
              // We will swallow the exception thrown from after if it is not test-aborting and exception was already thrown by before or test itself.
            }
          }
        } else runStatus
      thrownException match {
        case Some(e) => throw e
        case None    =>
      }
      statusToReturn
    } catch {
      case laterException: Exception =>
        thrownException match { // If both before/run and after throw an exception, report the earlier exception
          case Some(earlierException) => throw earlierException
          case None                   => throw laterException
        }
    }
  }

}

private object IntegrationTestSpecImpl {

  final private def anExceptionThatShouldCauseAnAbort(throwable: Throwable): Boolean =
    throwable match {
      case _: AnnotationFormatError | _: CoderMalfunctionError | _: FactoryConfigurationError | _: LinkageError |
          _: ThreadDeath | _: TransformerFactoryConfigurationError | _: VirtualMachineError =>
        true
      // Don't use AWTError directly because it doesn't exist on Android, and a user
      // got ScalaTest to compile under Android.
      case e if e.getClass.getName == "java.awt.AWTError" => true
      case _                                              => false
    }

}
