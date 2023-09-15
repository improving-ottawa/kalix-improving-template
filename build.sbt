lazy val kalixImprovingTemplate = (project in file(".")).settings(
  Test / test := {
    val _ = (Test / g8Test).toTask("").value
  },
  scriptedLaunchOpts ++= List(
    "-Xms1024m",
    "-Xmx1024m",
    "-Xss2m",
    "-Dfile.encoding=UTF-8"
  ),
  resolvers += Resolver.url(
    "typesafe",
    url("https://repo.typesafe.com/typesafe/ivy-releases/")
  )(Resolver.ivyStylePatterns)
)

Global / onChangedBuildSource := ReloadOnSourceChanges
