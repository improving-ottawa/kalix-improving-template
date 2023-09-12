package com.improving.sbtkalix

import sbt.*

object SbtKalixImprovingPlugin extends AutoPlugin {

  val helpers: Seq[AutoPluginHelper] = {
    Seq(
    )
  }
  val autoPlugins: Seq[AutoPlugin] = {
    helpers.flatMap(_.autoPlugins)// :+ GitPlugin
  }

  override def requires: Plugins = {
    autoPlugins.foldLeft(empty) { (b, plugin) =>
      b && plugin
    }
  }
}
