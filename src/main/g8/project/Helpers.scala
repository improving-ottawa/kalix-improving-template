import Dependencies._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin
import sbt.Keys._
import sbt._
import sbtdynver.DynVerPlugin.autoImport.dynverSeparator
import sbtprotoc.ProtocPlugin.autoImport.PB
import scalapb.GeneratorOption.{FlatPackage, RetainSourceCodeInfo, SingleLineToProtoString}

object Compilation {

  def scala(project: Project): Project = {
    project.settings(
      ThisBuild / dynverSeparator := "-",
      run / fork := true,
      run / envVars += ("HOST", "0.0.0.0"),
      Compile / run := {
        // needed for the proxy to access the user function on all platforms
        sys.props += "kalix.user-function-interface" -> "0.0.0.0"
        (Compile / run).evaluated
      },
      scalaVersion := "2.13",
      Compile / scalacOptions ++= Seq(
        "-release:11",
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

//  def dockerConfiguration(project: Project): Project = {
//    project.settings(
//      dockerBaseImage := "docker.io/library/eclipse-temurin:17",
//      dockerUsername := sys.props.get("docker.username"),
//      dockerRepository := sys.props.get("docker.registry"),
//      dockerUpdateLatest := true,
//      // dockerAlias := dockerAlias.value.withTag(Some("latest"))
//      dockerExposedPorts ++= Seq(8080),
//      dockerBuildCommand := {
//        if (sys.props("os.arch") != "amd64") {
//          // use buildx with platform to build supported amd64 images on other CPU architectures
//          // this may require that you have first run 'docker buildx create' to set docker buildx up
//          dockerExecCommand.value ++ Seq(
//            "buildx",
//            "build",
//            "--platform=linux/amd64",
//            "--load"
//          ) ++ dockerBuildOptions.value :+ "."
//        } else dockerBuildCommand.value
//      }
//    )
//  }
}

object Kalix {

  def service(componentName: String, port: Int = 8080)(
      project: Project
  ): Project = {
    project
      //.enablePlugins(KalixPlugin, JavaAppPackaging, DockerPlugin)
      .configure(Compilation.scala)
      .configure(Testing.scalaTest)
      .configure(Packaging.docker)
      .settings(
        name := componentName,
        run / fork := true,
        run / javaOptions += s"-Dkalix.user-function-port=$port",
        libraryDependencies ++= utilityDependencies ++ loggingDependencies ++ scalaPbDependencies ++ scalaPbValidationDependencies,
        Compile / managedSourceDirectories ++= Seq(
          target.value / "scala-2.13" / "akka-grpc",
          target.value / "scala-2.13" / "src_managed"
        )
      )
  }

  def library(componentName: String)(project: Project): Project = {
    project
      .configure(Compilation.scala)
      .configure(Compilation.scalapbCodeGen)
      .configure(Testing.scalaTest)
      .settings(
        name := componentName,
        run / fork := true,
        libraryDependencies ++= loggingDependencies,
        Compile / managedSourceDirectories ++= Seq(
          target.value / "scala-2.13" / "akka-grpc",
          target.value / "scala-2.13" / "src_managed"
        )
      )
  }

  def dependsOn(dependency: Project, name: String)(
      project: Project
  ): Project = {
    project
      .dependsOn(dependency % "compile->compile;test->test")
  }

  def loadTest(testName: String)(
      project: Project
  ): Project = {
    project
      .settings(
        name := testName,
        run / fork := true,
        libraryDependencies ++= loadTestDependencies,
        Compile / scalacOptions ++= Seq(
          "-encoding",
          "UTF-8",
          "-target:jvm-1.8",
          "-deprecation",
          "-feature",
          "-unchecked",
          "-language:implicitConversions",
          "-language:postfixOps"
        )
      )
  }
}
