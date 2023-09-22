ThisBuild / organization := "com.improving"
ThisBuild / scalaVersion := "2.13.10"

name := "kalix-improving-template"

organization := "improving"
organizationHomepage := Some(url("https://www.improving.com/"))

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val common: Project = project
  .in(file("common"))
  .configure(Config.Kalix.library)

lazy val utils: Project = project
  .in(file("utils"))
  .configure(Config.Kalix.library)

lazy val service1 = project
  .in(file("service1"))
  .configure(Config.Kalix.service)
  .configure(Config.Kalix.dependsOn(common))
  .configure(Config.Kalix.dependsOn(utils))

lazy val service2 = project
  .in(file("service2"))
  .configure(Config.Kalix.service)
  .configure(Config.Kalix.dependsOn(common))
  .configure(Config.Kalix.dependsOn(utils))

lazy val gateway = project
  .in(file("gateway"))
  .configure(Config.Kalix.service)
  .configure(Config.Kalix.dependsOn(service1))
  .configure(Config.Kalix.dependsOn(service2))

lazy val root = project
  .in(file("."))
  .settings(
    publish := {},
    publishLocal := {},
    publishTo := Some(Resolver.defaultLocal)
  )
  .aggregate(
    gateway
  )
