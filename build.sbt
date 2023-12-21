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
  .aggregate(
    design,
    common,
    utils,
    `integration-testkit`,
    `integration-testkit-tests`,
    service3,
    `bounded-context`,
    gateway,
    extensions
  )

// ***
// Shared / common libraries (not intended for customization)
// ***

lazy val utils: Project = project
  .in(file("utils"))
  .configure(Config.AsProjectType.basicLibrary)
  .configure(Config.withDeps(Dependencies.kalixScalaSdk, Dependencies.scodecBits))
  .configure(Config.withDepsPackage(Dependencies.jwtSupportPackage))

lazy val common: Project = project
  .in(file("common"))
  .configure(Config.AsProjectType.protobufLibrary)
  .configure(Config.withDeps(Dependencies.javaLibRecur))
  .dependsOn(utils)

lazy val `integration-testkit`: Project = project
  .in(file("integration-testkit"))
  .configure(Config.AsProjectType.basicLibrary)
  .configure(Config.withDeps(Dependencies.testContainers, Dependencies.kalixScalaTestkit))
  .configure(Config.withDeps(Dependencies.scalatestCore))
  .dependsOn(utils)

lazy val extensions: Project = project
  .in(file("extensions"))
  .configure(Config.AsProjectType.basicLibrary)
  .configure(Config.withDeps(Dependencies.pencilSmtp, Dependencies.slf4jCats, Dependencies.testContainers))
  .configure(Config.withDepsPackage(Dependencies.iamDepsPackage))
  .configure(Config.withDepsPackage(Dependencies.cachingDependencies))
  .configure(Config.withDepsPackage(Dependencies.grpc))
  .dependsOn(common)

// ***
// Demo specific libraries and services (customize these!)
// ***

lazy val design: Project = project
  .in(file("design"))
  .configure(Config.riddl(appName))

lazy val service3 = project
  .in(file("service3"))
  .configure(Config.AsKalix.library)
  .dependsOn(common, utils)

lazy val `bounded-context` = project
  .in(file("bounded-context"))
  .configure(Config.AsKalix.service)
  .configure(Config.withDepsPackage(Dependencies.csvParsingDepsPackage))
  .dependsOn(utils, common, service3)

lazy val gateway = project
  .in(file("gateway"))
  .configure(Config.AsKalix.service)
  .dependsOn(`bounded-context`, extensions, `integration-testkit` % Test)

lazy val `scheduled-tasks` = project
  .in(file("scheduled-tasks"))
  .configure(Config.AsKalix.service)
  .dependsOn(`bounded-context`)

lazy val `integration-testkit-tests`: Project = project
  .in(file("integration-testkit-tests"))
  .configure(Config.AsProjectType.basicLibrary)
  .dependsOn(`integration-testkit`, `bounded-context`, gateway)
