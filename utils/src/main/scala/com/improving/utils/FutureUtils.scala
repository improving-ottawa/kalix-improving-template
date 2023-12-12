package com.improving.utils

import scala.concurrent.Future
import scala.language.implicitConversions

trait FutureUtils {
  import FutureUtils._

  implicit final def futureCompanionExtensions(comp: Future.type): FutureCompanionExtensions =
    new FutureCompanionExtensions(comp)

}

object FutureUtils extends FutureUtils {

  final class FutureCompanionExtensions(private val comp: Future.type) extends AnyVal {

    def fromEither[A](either: Either[Throwable, A]): Future[A] =
      either.fold[Future[A]](Future.failed, Future.successful)

  }

}
