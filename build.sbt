ThisBuild / organization := "com.improving"
ThisBuild / scalaVersion := "2.13.10"

name := "kalix-improving-template"

organization := "improving"
organizationHomepage := Some(url("https://www.improving.com/"))

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val common: Project = project
  .disablePlugins(KalixPlugin)
  .in(file("common"))
  .configure(Kalix.library("common"))

lazy val utils: Project = project
  .disablePlugins(KalixPlugin)
  .in(file("utils"))
  .configure(Kalix.library("utils"))

lazy val service1 = project
  .in(file("service1"))
  .configure(Kalix.service("service2"))
  .configure(Kalix.dependsOn(common, "common"))
  .configure(Kalix.dependsOn(utils, "utils"))

lazy val service2 = project
  .in(file("service2"))
  .configure(Kalix.service("service2"))
  .configure(Kalix.dependsOn(common, "common"))
  .configure(Kalix.dependsOn(utils, "utils"))

lazy val root = project
  .in(file("."))
  .settings(
    publish := {},
    publishLocal := {},
    publishTo := Some(Resolver.defaultLocal)
  )
  .aggregate(
    service1,
    service2
  )
