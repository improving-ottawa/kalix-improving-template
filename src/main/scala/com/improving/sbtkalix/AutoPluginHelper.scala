package com.improving.sbtkalix

import sbt.*

/** A Little Help For AutoPlugins
  * This trait just provides some definitions and makes it easier to set up the
  * plugin requirements. Just list  the plugins upon which your plugin is
  * dependent in the autoPlugins method and the rest is taken care of.
  */
trait AutoPluginHelper {

  /** The AutoPlugins that we depend upon */
  def autoPlugins: Seq[AutoPlugin] = {
    Seq.empty[AutoPlugin]
  }

  /** The Configurations to add to each project that activates this AutoPlugin.
    */
  def projectConfigurations: Seq[Configuration] = Nil

  /** Define default settings for ReactificPlugin at The Settings to add in the
    * scope of each project that activates this
    * AutoPlugin.
    */
  def projectSettings: Seq[Setting[?]] = Nil

  /** The Settings to add to the build scope for each project that activates
    * this AutoPlugin. The settings returned here are guaranteed to be added
    * to a given build scope only once regardless of how many projects for
    * that build activate this AutoPlugin.
    */
  def buildSettings: Seq[Setting[?]] = Nil

  /** The Settings to add to the global scope exactly once if any project
    * activates this AutoPlugin.
    */
  def globalSettings: Seq[Setting[?]] = Nil

}
