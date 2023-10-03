lazy val appName: String = "example"

ThisBuild / organization := s"com.$appName"

Global / onChangedBuildSource := ReloadOnSourceChanges

name := "kalix-improving-template"

organization := "improving"
organizationHomepage := Some(url("https://www.improving.com/"))

lazy val root = project
  .in(file("."))
  .settings(
    publish := {},
    publishLocal := {},
    publishTo := Some(Resolver.defaultLocal)
  )
  .aggregate(
    design,
    common,
    utils,
    service1,
    service2,
    gateway
  )

lazy val design: Project = project
  .in(file("design"))
  .configure(Config.withRiddl(appName))

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
