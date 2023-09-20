import Dependencies._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin
import sbt.Keys._
import sbt._
import sbtdynver.DynVerPlugin.autoImport.dynverSeparator
import sbtprotoc.ProtocPlugin.autoImport.PB
import sbt.Keys.{libraryDependencies, _}
import scalapb.GeneratorOption.{FlatPackage, RetainSourceCodeInfo, SingleLineToProtoString}

object Compilation {

  def scala(project: Project): Project = {
    project.settings(
      ThisBuild / dynverSeparator := "-",
      run / fork := true,
      run / envVars += ("HOST", "0.0.0.0"),
      run / javaOptions ++= Seq(
        "-Dkalix.user-function-interface=0.0.0.0",
        "-Dlogback.configurationFile=logback-dev-mode.xml"
      ),
      scalaVersion := "2.13",
      Compile / scalacOptions ++= Seq(
        "-target:11",
        "-deprecation",
        "-feature",
        "-unchecked",
        "-Xlog-reflective-calls",
        "-Xlint"
      ),
      Compile / javacOptions ++= Seq(
        "-Xlint:unchecked",
        "-Xlint:deprecation",
        "-parameters" // for Jackson
      )
    )
  }

  def scalapbCodeGen(project: Project): Project = {
    project.settings(
      libraryDependencies ++= scalaPbDependencies,
      Compile / PB.targets := Seq(
        scalapb.gen(
          FlatPackage,
          SingleLineToProtoString,
          RetainSourceCodeInfo
        ) -> (Compile / sourceManaged).value / "scalapb",
        scalapb.validate.gen(
          FlatPackage,
          SingleLineToProtoString,
          RetainSourceCodeInfo
        ) -> (Compile / sourceManaged).value / "scalapb"
      ),
      libraryDependencies += scalaPbCompilerPlugin
    )
  }
}

object Testing {
  def scalaTest(project: Project): Project = {
    project.settings(
      Test / parallelExecution := false,
      Test / testOptions += Tests.Argument("-oDF"),
      Test / logBuffered := false,
      libraryDependencies ++= basicTestingDependencies ++ jsonDependencies
    )
  }
}

object Packaging {

  def docker(project: Project): Project = {
    project.settings(
      dockerBaseImage := "docker.io/library/adoptopenjdk:11-jre-hotspot",
      dockerUsername := sys.props.get("docker.username"),
      dockerRepository := sys.props.get("docker.registry"),
      dockerUpdateLatest := true,
      dockerExposedPorts ++= Seq(8080),
      dockerBuildCommand := {
        if (sys.props("os.arch") != "amd64") {
          // use buildx with platform to build supported amd64 images on other CPU architectures
          // this may require that you have first run 'docker buildx create' to set docker buildx up
          dockerExecCommand.value ++ Seq(
            "buildx",
            "build",
            "--platform=linux/amd64",
            "--load"
          ) ++ dockerBuildOptions.value :+ "."
        } else dockerBuildCommand.value
      }
    )
  }

}
