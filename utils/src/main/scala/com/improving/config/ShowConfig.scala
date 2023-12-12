package com.improving.config

import com.improving.utils.StringPrinter

import cats.Show

/**
  * [[ShowConfig]] is a type-class for configuration data-types which are `showable` (can be rendered to text).
  */
trait ShowConfig[A] extends Show[A] {
  def print(cfg: A, printer: StringPrinter): StringPrinter

  final def show(cfg: A): String = print(cfg, StringPrinter(indentSize = 4)).result
}

object ShowConfig {
  import StringPrinter.PrinterEndo

  final def show[A : ShowConfig](cfg: A): String = implicitly[ShowConfig[A]].show(cfg)

  /** Creates a new instance of [[ShowConfig]] from the provided function. */
  def apply[A](f: A => PrinterEndo): ShowConfig[A] =
    (cfg: A, printer: StringPrinter) => f(cfg)(printer)

}
