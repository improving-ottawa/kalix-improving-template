import Dependencies._
import akka.grpc.sbt.AkkaGrpcPlugin
import akka.grpc.sbt.AkkaGrpcPlugin.autoImport.akkaGrpcCodeGeneratorSettings
import com.reactific.riddl.sbt.plugin.RiddlSbtPlugin
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.*
import com.typesafe.sbt.SbtNativePackager.autoImport.maintainer
import sbt.Keys.*
import sbt.{Compile, _}
import scoverage.ScoverageKeys.{*, coverageFailOnMinimum}
import sbtdynver.DynVerPlugin.autoImport.dynverSeparator
import sbtdynver.DynVerPlugin.autoImport.dynverVTagPrefix
import wartremover.WartRemover.autoImport.*

import scala.collection.immutable.Seq
import kalix.sbt.KalixPlugin
import protocbridge.Target
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys.buildInfoKeys
import sbtbuildinfo.BuildInfoKeys.buildInfoObject
import sbtbuildinfo.BuildInfoKeys.buildInfoOptions
import sbtbuildinfo.BuildInfoKeys.buildInfoPackage
import sbtbuildinfo.BuildInfoKeys.buildInfoUsePackageAsPath
import sbtbuildinfo.BuildInfoOption.BuildTime
import sbtbuildinfo.BuildInfoOption.ToJson
import sbtbuildinfo.BuildInfoOption.ToMap
import sbtbuildinfo.BuildInfoPlugin
import sbtprotoc.ProtocPlugin.autoImport.PB
import scalapb.GeneratorOption
import scalapb.GeneratorOption.{FlatPackage, _}
import com.reactific.riddl.sbt.plugin.RiddlSbtPlugin
import com.reactific.riddl.sbt.plugin.RiddlSbtPlugin.autoImport.*

import java.net.URI
import java.util.Calendar

object Config {
  def withInfo(p: Project): Project = {
    p.settings(
      ThisBuild / maintainer := "reid@ossum.biz",
      ThisBuild / organization := "com.ossum.amenities",
      ThisBuild / organizationHomepage := Some(URI.create("https://reactific.com/").toURL),
      ThisBuild / organizationName := "Ossum Inc.",
      ThisBuild / startYear := Some(2019),
      ThisBuild / licenses +=
        (
          "Apache-2.0",
          new URI("https://www.apache.org/licenses/LICENSE-2.0.txt").toURL
        ),
      ThisBuild / versionScheme := Option("early-semver"),
      ThisBuild / dynverVTagPrefix := false,
      run / fork := true,
      run / envVars += ("HOST", "0.0.0.0"),
      run / javaOptions ++= Seq("-Dlogback.configurationFile=logback-dev-mode.xml"),

      // NEVER  SET  THIS: version := "0.1"
      // IT IS HANDLED BY: sbt-dynver
      ThisBuild / dynverSeparator := "-",
    )
  }

  object Scala {

    lazy val scala_2_options: Seq[String] =
      Seq(
        "-deprecation",
        "-feature",
        // "-new-syntax",
        "-release:11",
        "-unchecked",
        "-Xlog-reflective-calls",
        "-Xlint",
        // "-explain",
        // "-explain-types",
        // "-Werror",
      )

    lazy val java_options: Seq[String] = Seq(
      "-Xlint:unchecked",
      "-Xlint:deprecation",
      "-parameters" // for Jackson
    )

    def withScala2(p: Project): Project = {
      p.configure(withInfo)
        .settings(
          ThisBuild / dynverSeparator := "-",
          scalaVersion := "2.13.10", // "3.3.1-RC7",
          scalacOptions := scala_2_options,
          apiURL := Some(url("https://riddl.tech/apidoc/")),
          autoAPIMappings := true,
          Test / parallelExecution := false,
          Test / testOptions += Tests.Argument("-oDF"),
          Test / logBuffered := false,
          libraryDependencies ++= Dependencies.basicTestingDependencies ++ Dependencies.jsonDependencies
        )
    }

    lazy val scala_3_options: Seq[String] =
      Seq(
        "-deprecation",
        "-feature",
        "-new-syntax",
        "-explain",
        "-explain-types",
        "-Werror",
        "-pagewidth",
        "120"
      )

    def scala_3_doc_options(version: String): Seq[String] = {
      Seq(
        "-deprecation",
        "-feature",
        "-groups",
        "-project:RIDDL",
        "-comment-syntax:wiki",
        s"-project-version:$version",
        "-siteroot:doc/src/hugo/static/apidoc",
        "-author",
        "-doc-canonical-base-url:https://riddl.tech/apidoc"
      )
    }

    def withScala3(p: Project): Project = {
      p.configure(withInfo)
        .settings(
          scalaVersion := "3.3.1",
          scalacOptions := scala_3_options,
          Compile / doc / scalacOptions := scala_3_doc_options((compile / scalaVersion).value),
          autoAPIMappings := true
        )
        .configure(withWartRemover)
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

    final val defaultPercentage: Int = 50

    def withCoverage(percent: Int = defaultPercentage)(p: Project): Project = {
      p.settings(
        coverageFailOnMinimum := true,
        coverageMinimumStmtTotal := percent,
        coverageMinimumBranchTotal := percent,
        coverageMinimumStmtPerPackage := percent,
        coverageMinimumBranchPerPackage := percent,
        coverageMinimumStmtPerFile := percent,
        coverageMinimumBranchPerFile := percent,
        coverageExcludedPackages := "<empty>"
      )
    }

    def withBuildInfo(
        homePage: String,
        orgName: String,
        packageName: String,
        objName: String = "BuildInfo",
        baseYear: Int = 2023
    )(p: Project): Project = {
      p.enablePlugins(BuildInfoPlugin)
        .settings(
          buildInfoObject := objName,
          buildInfoPackage := packageName,
          buildInfoOptions := Seq(ToMap, ToJson, BuildTime),
          buildInfoUsePackageAsPath := true,
          buildInfoKeys ++= Seq[BuildInfoKey](
            name,
            version,
            description,
            organization,
            organizationName,
            BuildInfoKey.map(organizationHomepage) { case (k, v) =>
              k -> v.get.toString
            },
            BuildInfoKey.map(homepage) { case (k, v) =>
              "projectHomepage" -> v.map(_.toString).getOrElse(homePage)
            },
            BuildInfoKey.map(startYear) { case (k, v) =>
              k -> v.map(_.toString).getOrElse(baseYear.toString)
            },
            BuildInfoKey.map(startYear) { case (k, v) =>
              "copyright" -> s"© ${v.map(_.toString).getOrElse(baseYear.toString)}-${Calendar
                  .getInstance()
                  .get(Calendar.YEAR)} $orgName}"
            },
            scalaVersion,
            sbtVersion,
            BuildInfoKey.map(scalaVersion) { case (k, v) =>
              val version = if (v.head == '2') {
                v.substring(0, v.lastIndexOf('.'))
              } else v
              "scalaCompatVersion" -> version
            },
            BuildInfoKey.map(licenses) { case (k, v) =>
              k -> v.map(_._1).mkString(", ")
            }
          )
        )
    }

    def withWartRemover(proj: Project): Project = {
      proj
        .enablePlugins(wartremover.WartRemover)
        .settings(
          Compile / compile / wartremoverErrors ++= Warts.all,
          // Compile / compile / wartremoverErrors ++= Warts.allBut(Wart.Any, Wart.Nothing, Wart.Serializable)
          // wartremoverWarnings += Wart.Nothing,
          // wartremoverWarnings ++= Seq(Wart.Any, Wart.Serializable)
        )
    }
  }

  def withDocker(proj: Project): Project = {
    proj
      .enablePlugins(DockerPlugin)
      .settings(
        dockerBaseImage := "docker.io/library/adoptopenjdk:11-jre-hotspot",
        dockerUsername := sys.props.get("docker.username"),
        dockerRepository := sys.props.get("docker.registry"),
        dockerUpdateLatest := true,
        dockerExposedPorts ++= Seq(8080),
        dockerBuildCommand := {
          val arch = sys.props("os.arch")
          if (arch != "amd64" && !arch.contains("x86")) {
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

  def withRiddl(appName: String)(proj: Project): Project = {
    proj
      .enablePlugins(RiddlSbtPlugin)
      .configure(Config.Scala.withScala3)
      .settings(
        scalaVersion := "3.3.1",
        riddlcConf := file(s"design/src/main/riddl/$appName.conf"),
        riddlcMinVersion := "0.27.0",
        riddlcOptions := Seq("--show-times", "--verbose"),
      )
  }

  object ScalaPB {

    private val generator_options: Seq[GeneratorOption] = Seq(
      FlatPackage,
      SingleLineToProtoString,
      RetainSourceCodeInfo
    )

    def protoGenValidate(project: Project): Project = {
      project
        .enablePlugins(AkkaGrpcPlugin)
        .settings(
          libraryDependencies ++= Dependencies.scalaPbValidationDependencies,
          Compile / PB.targets ++= Seq[Target](
            protocbridge.Target(
              scalapb.validate.preprocessor(FlatPackage),
              (Compile / akkaGrpcCodeGeneratorSettings / target).value
            ),
            protocbridge
              .Target(scalapb.validate.gen(FlatPackage), (Compile / akkaGrpcCodeGeneratorSettings / target).value)
          )
        )
    }
  }

  val minCoverage: Int = 80

  object Kalix {
    def service(proj: Project): Project = {
      proj
        .enablePlugins(KalixPlugin, JavaAppPackaging, DockerPlugin)
        .configure(Config.Scala.withCoverage(minCoverage))
        .configure(Config.Scala.withScala2)
        .configure(Config.ScalaPB.protoGenValidate)
        .configure(Config.withDocker)
        .settings(
          exportJars := true,
          run / envVars += ("HOST", "0.0.0.0"),
          // needed for the proxy to access the user function on all platforms
          run / javaOptions ++= Seq(
            "-Dkalix.user-function-interface=0.0.0.0",
            "-Dlogback.configurationFile=logback-dev-mode.xml"
          ),
          run / fork := true,
          Global / cancelable := false, // ctrl-c
          libraryDependencies ++= Dependencies.testingDeps ++ Dependencies.grpc ++ Dependencies.loggingDependencies,
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

    def library(proj: Project): Project = {
      proj
        .enablePlugins(KalixPlugin, JavaAppPackaging)
        .configure(Config.Scala.withScala2)
        .configure(Config.Scala.withCoverage(minCoverage))
        .configure(Config.ScalaPB.protoGenValidate)
        .settings(
          libraryDependencies ++= testingDeps
        )
    }

    def dependsOn(dependency: Project)(
        project: Project
    ): Project = {
      project
        .dependsOn(dependency)
        .dependsOn(dependency % "protobuf;compile->compile;test->test")
    }

  }

  def riddl(appName: String)(proj: Project): Project = {
    proj
      .enablePlugins(RiddlSbtPlugin)
      .settings(
        // riddlcConf := file(s"design/src/main/riddl/$appName.conf"),
        riddlcMinVersion := "0.25.0",
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
