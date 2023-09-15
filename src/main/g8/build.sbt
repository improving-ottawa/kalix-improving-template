ThisBuild / organization := "com.improving"
ThisBuild / scalaVersion := "2.13.10"

name := "kalix-improving"

organization := "improving"
organizationHomepage := Some(url("https://www.improving.com/"))

lazy val kalixImproving = (project in file(".")).configure(Kalix.service("$name$"))
