ThisBuild / organization := "com.improving"
ThisBuild / scalaVersion := "2.13.10"

name := "kalix-improving-template"

organization := "improving"
organizationHomepage := Some(url("https://www.improving.com/"))

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val common: Project = project
  .in(file("common"))
  .configure(Kalix.library("common"))

lazy val utils: Project = project
  .in(file("utils"))
  .configure(Kalix.library("utils"))

lazy val service1 = project
  .in(file("service1"))
  .configure(Kalix.service("service2"))
  .configure(Kalix.dependsOn(common))
  .configure(Kalix.dependsOn(utils))

lazy val service2 = project
  .in(file("service2"))
  .configure(Kalix.service("service2"))
  .configure(Kalix.dependsOn(common))
  .configure(Kalix.dependsOn(utils))

lazy val gateway = project
  .in(file("gateway"))
  .configure(Kalix.service("ui"))
  .configure(Kalix.dependsOn(service1))
  .configure(Kalix.dependsOn(service2))

lazy val root = project
  .in(file("."))
  .settings(
    publish := {},
    publishLocal := {},
    publishTo := Some(Resolver.defaultLocal)
  )
  .aggregate(
    service1,
    service2,
    gateway
  )
