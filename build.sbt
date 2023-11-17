lazy val appName: String = "example"
ThisBuild / organization := s"com.$appName"

Global / onChangedBuildSource := ReloadOnSourceChanges

name := "kalix-improving-template"

organization         := "improving"
organizationHomepage := Some(url("https://www.improving.com/"))

// These are the projects which are Kalix services, meaning precisely they will have
// docker images built and they are deployable to the Kalix cloud.
// Note: if you write a new Kalix service, make sure it is added to this list!
lazy val kalixServices = List[Project](
  `bounded-context`,
  gateway
)

// The "root" container project
lazy val root = project
  .in(file("."))
  .settings(
    publish                    := {},
    publishLocal               := {},
    publishTo                  := Some(Resolver.defaultLocal),
    // Publishes docker images to Kalix container registry
    KalixEnv.publishContainers := { KalixEnv.publishProjectContainers(kalixServices).value },
    // Publishes each service to Kalix with the `latest` image tag.
    KalixEnv.deployServices    := { KalixEnv.deployProjectServices(kalixServices).value },
    // Publish containers + deploy services (combo command)
    KalixEnv.publishAndDeploy  := { KalixEnv.deployServices.dependsOn(KalixEnv.publishContainers).value }
  )
  .aggregate(design, common, utils, service3, `bounded-context`, gateway, extensions)

lazy val design: Project = project
  .in(file("design"))
  .configure(Config.riddl(appName))

lazy val utils: Project = project
  .in(file("utils"))
  .configure(Config.Kalix.baseLibrary)
  .configure(Config.withDeps(Dependencies.kalixScalaSdk))
  .configure(Config.withDepsPackage(Dependencies.jwtSupportPackage))
  .configure(Config.withDepsPackage(Dependencies.httpDepsPackage))
  .configure(Config.withDepsPackage(Dependencies.functionalDepsPackage))

lazy val common: Project = project
  .in(file("common"))
  .configure(Config.Kalix.baseLibrary)
  .configure(Config.Kalix.dependsOn(utils))
  .configure(Config.withDeps(Dependencies.javaLibRecur))
  .configure(Config.withDepsPackage(Dependencies.scalaPbGoogleCommonProtos))

lazy val service3 = project
  .in(file("service3"))
  .configure(Config.Kalix.kalixLibrary)
  .configure(Config.Kalix.dependsOn(common))
  .configure(Config.Kalix.dependsOn(utils))

lazy val extensions: Project = project
  .in(file("extensions"))
  .configure(Config.Kalix.baseLibrary)
  .configure(Config.Kalix.dependsOn(common))
  .configure(Config.withDeps(Dependencies.pencilSmtp, Dependencies.slf4jCats, Dependencies.testContainers))

lazy val `bounded-context` = project
  .in(file("bounded-context"))
  .configure(Config.Kalix.kalixLibrary)
  .configure(Config.Kalix.dependsOn(common))
  .configure(Config.Kalix.dependsOn(utils))
  .configure(Config.Kalix.dependsOn(service3))
  .configure(Config.withDepsPackage(Dependencies.csvParsingDepsPackage))
  .settings(Compile / run / fork := false)

lazy val gateway = project
  .in(file("gateway"))
  .configure(Config.Kalix.service)
  .configure(Config.Kalix.dependsOn(`bounded-context`))

lazy val `scheduled-tasks` = project
  .in(file("scheduled-tasks"))
  .configure(Config.Kalix.service)
  .configure(Config.Kalix.dependsOn(boundedContext))
