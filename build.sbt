lazy val appName: String = "example"
ThisBuild / organization := s"com.$appName"

Global / onChangedBuildSource := ReloadOnSourceChanges

name := "example"

organization         := "improving"
organizationHomepage := Some(url("https://www.improving.com/"))

// These are the projects which are Kalix services, meaning precisely they will have
// docker images built and they are deployable to the Kalix cloud.
// Note: if you write a new Kalix service, make sure it is added to this list!
lazy val kalixServices = List[Project](
  boundedContext,
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
  .aggregate(design, common, utils, boundedContext, gateway)

lazy val design: Project = project
  .in(file("design"))
  .configure(Config.riddl(appName))

lazy val utils: Project = project
  .in(file("utils"))
  .configure(Config.Kalix.baseLibrary)
  .configure(Config.withDeps(Dependencies.kalixScalaSdk))
  .configure(Config.withDepsPackage(Dependencies.jwtSupportPackage))
  .configure(Config.withDepsPackage(Dependencies.httpDepsPackage))

lazy val common: Project = project
  .in(file("common"))
  .configure(Config.Kalix.baseLibrary)
  .configure(Config.Kalix.dependsOn(utils))
  .configure(Config.withDeps(Dependencies.javaLibRecur))
  .configure(Config.withDepsPackage(Dependencies.functionalDepsPackage))
  .configure(Config.withDepsPackage(Dependencies.scalaPbGoogleCommonProtos))

lazy val extensions: Project = project
  .in(file("extensions"))
  .configure(Config.Kalix.baseLibrary)
  .configure(Config.Kalix.dependsOn(common))
  .configure(Config.withDeps(Dependencies.pencilSmtp, Dependencies.slf4jCats, Dependencies.testContainers))

lazy val boundedContext = project
  .in(file("bounded-context"))
  .configure(Config.Kalix.kalixLibrary)
  .configure(Config.Kalix.dependsOn(common))
  .configure(Config.Kalix.dependsOn(utils))
  .configure(Config.withDepsPackage(Dependencies.csvParsingDepsPackage))
  .settings(Compile / run / fork := false)

lazy val gateway = project
  .in(file("gateway"))
  .configure(Config.Kalix.service)
  .configure(Config.Kalix.dependsOn(boundedContext))
