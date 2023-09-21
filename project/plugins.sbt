addSbtPlugin("io.kalix" % "sbt-kalix" % "1.3.3")
addSbtPlugin("io.gatling" % "gatling-sbt" % "4.5.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.0.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.1")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.6")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.0-RC2")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")

libraryDependencies ++= Seq(
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value,
  "com.thesamet.scalapb" %% "compilerplugin" % "0.11.13",
  "com.thesamet.scalapb" %% "scalapb-validate-codegen" % "0.3.4",
)

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
