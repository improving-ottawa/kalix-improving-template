import akka.grpc.sbt.AkkaGrpcPlugin
import akka.grpc.sbt.AkkaGrpcPlugin.autoImport.akkaGrpcCodeGeneratorSettings
import com.reactific.riddl.sbt.plugin.RiddlSbtPlugin
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.*
import sbt.Keys.*
import sbt.{Compile, _}
import scoverage.ScoverageKeys.{coverageFailOnMinimum, _}
import sbtdynver.DynVerPlugin.autoImport.dynverSeparator
import sbtdynver.DynVerPlugin.autoImport.dynverVTagPrefix

import scala.collection.immutable.Seq
import kalix.sbt.KalixPlugin
import kalix.sbt.KalixPlugin.autoImport.*
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
import com.reactific.riddl.sbt.plugin.RiddlSbtPlugin.autoImport.*
import org.scoverage.coveralls.Imports.CoverallsKeys.coverallsToken

import java.util.Calendar

import Dependencies._

object Config {

  def withInfo(p: Project): Project = {
    p.settings(
      ThisBuild / organization     := "io.example",
      // ThisBuild / organizationHomepage := Some(URI.create("https://???/").toURL),
      ThisBuild / organizationName := "example",
      ThisBuild / startYear        := Some(2023),
      ThisBuild / versionScheme    := Option("early-semver"),
      ThisBuild / dynverVTagPrefix := false,
      run / fork                   := true,
      run / envVars += ("HOST", "0.0.0.0"),
      run / javaOptions ++= Seq("-Dlogback.configurationFile=logback-dev-mode.xml"),

      // NEVER  SET  THIS: version := "0.1"
      // IT IS HANDLED BY: sbt-dynver
      ThisBuild / dynverSeparator := "-",
    )
  }

  def withDepsPackage(deps: scala.collection.Seq[ModuleID])(proj: Project): Project =
    proj.settings(libraryDependencies ++= deps)

  def withDeps(first: ModuleID, rest: ModuleID*)(proj: Project): Project =
    proj.settings(libraryDependencies ++= first +: rest)

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
          scalaVersion                := "2.13.12", // "3.3.1",
          scalacOptions               := scala_2_options,
          apiURL                      := Some(url("https://riddl.tech/apidoc/")),
          autoAPIMappings             := true,
          Test / parallelExecution    := false,
          Test / testOptions += Tests.Argument("-oDF"),
          Test / logBuffered          := false,
          libraryDependencies ++= Dependencies.basicTestingDependencies ++ Dependencies.jsonDependencies
        )
    }

    def scalapbCodeGen(project: Project): Project = {
      project.settings(
        libraryDependencies ++= scalaPbValidationDependencies,
        libraryDependencies += scalaPbCompilerPlugin,
        Compile / PB.targets := Seq(
          scalapb.gen(
            FlatPackage,
            SingleLineToProtoString,
            RetainSourceCodeInfo
          ) -> (Compile / sourceManaged).value,
          scalapb.validate.gen(
            FlatPackage,
            SingleLineToProtoString,
            RetainSourceCodeInfo
          ) -> (Compile / sourceManaged).value
        )
      )
    }

    final val defaultPercentage: Int = 50

    def withCoverage(percent: Int = defaultPercentage)(p: Project): Project = {
      p.settings(
        coverageFailOnMinimum           := false,
        coverageMinimumStmtTotal        := percent,
        coverageMinimumBranchTotal      := percent,
        coverageMinimumStmtPerPackage   := percent,
        coverageMinimumBranchPerPackage := percent,
        coverageMinimumStmtPerFile      := percent,
        coverageMinimumBranchPerFile    := percent,
        coverallsToken                  := Some("jOdrBeb97Y02GGAeLnuLx4Gmm7anBb2Z"),
        coverageExcludedPackages        := "<empty>"
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
          buildInfoObject           := objName,
          buildInfoPackage          := packageName,
          buildInfoOptions          := Seq(ToMap, ToJson, BuildTime),
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

  }

  def withDocker(proj: Project): Project = {
    proj
      .enablePlugins(DockerPlugin)
      .settings(
        dockerBaseImage      := "docker.io/library/adoptopenjdk:11-jre-hotspot",
        dockerUsername       := sys.props.get("docker.username"),
        dockerRepository     := sys.props.get("docker.registry"),
        dockerUpdateLatest := true,
        dockerExposedPorts ++= Seq(8080),
        dockerBuildCommand   := {
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
          libraryDependencies += Dependencies.scalaPbCompilerPlugin,
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

  object AsProjectType {

    // Base/basic library, with the following:
    //  - Akka/Akka streams
    //  - Cats / Cats Effect
    //  - STTP and related packages
    //  - BouncyCastle for better cryptography support
    //  - ScalaTest, ScalaCheck
    // Does not include anything related to Kalix
    def basicLibrary(proj: Project): Project =
      proj
        .configure(Config.Scala.withScala2)
        .configure(Config.Scala.withCoverage(minCoverage))
        .settings(addCompilerPlugin(CompilerPlugins.betterForComp))
        .settings(addCompilerPlugin(CompilerPlugins.kindProjector))
        .settings(
          resolvers += "Sonatype OSS Release Repository".at("https://oss.sonatype.org/content/repositories/releases/"),
          libraryDependencies ++=
            akkaDepsPackage ++
            testingDeps ++
            bouncyCastleCryptoPackage ++
            functionalDepsPackage ++
            httpDepsPackage
        )

    // Extends `basicLibrary` to include the ability to generate code based on protobuf definitions
    // Note: the protobuf definitions should be under "src/main/proto" or "src/test/proto"
    def protobufLibrary(proj: Project): Project =
      basicLibrary(proj)
        .configure(Config.Scala.scalapbCodeGen)
        .enablePlugins(sbtprotoc.ProtocPlugin)
        .settings(
          libraryDependencies ++= Dependencies.scalaPbGoogleCommonProtos,
          Compile / PB.protoSources += { (Compile / sourceDirectory).value / "proto" },
          Test / PB.protoSources += { (Test / sourceDirectory).value / "proto" },
        )

  }

  object AsKalix {

    // Defines a Kalix component library, which is also a `AsProjectType.basicLibrary`, but customized for Kalix
    def library(proj: Project): Project = {
      AsProjectType.basicLibrary(proj)
        .enablePlugins(KalixPlugin)
        .configure(Config.ScalaPB.protoGenValidate)
        .settings(
          libraryDependencies ++= akkaDepsPackage ++
            akkaKalixServiceDepsPackage ++
            scalaPbGoogleCommonProtos ++
            kalixScalaPbDependencies,
          runAll := {
            val logger = streams.value.log
            logger.error("You cannot run a library!")
          }
        )
    }

    def service(proj: Project): Project = {
      AsProjectType.basicLibrary(proj)
        .enablePlugins(KalixPlugin, JavaAppPackaging, DockerPlugin)
        .configure(Config.ScalaPB.protoGenValidate)
        .configure(Config.withDocker)
        .settings(
          dockerRepository := Some(KalixEnv.containerRepository),
          dockerAliases    := {
            val packageName  = (proj / name).value
            val projVersion  = (proj / version).value
            val updatedAlias = DockerAlias(
              registryHost = Some(KalixEnv.containerRepository),
              username = None,
              name = s"${KalixEnv.organizationName}/${KalixEnv.projectName}/$packageName",
              tag = Some(projVersion)
            )

            Seq(
              // Updated docker alias
              updatedAlias,
              // With the `latest` tag
              updatedAlias.withTag(Some("latest"))
            )
          }
        )
        .settings(
          exportJars := true,
          run / envVars += ("HOST", "0.0.0.0"),
          // needed for the proxy to access the user function on all platforms
          run / javaOptions ++= Seq(
            "-Dkalix.user-function-interface=0.0.0.0",
            "-Dlogback.configurationFile=logback-dev-mode.xml"
          ),
          run / fork          := true,
          Global / cancelable := false, // ctrl-c
          libraryDependencies ++= (Dependencies.akkaDepsPackage ++
            Dependencies.akkaKalixServiceDepsPackage ++
            Dependencies.scalaPbGoogleCommonProtos ++
            Dependencies.kalixScalaPbDependencies ++
            Dependencies.grpc ++
            Dependencies.loggingDependencies ++
            Dependencies.integrationTestDependencies
          ),
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
        .settings(
          // Publishes docker images to Kalix container registry
          KalixEnv.publishContainers := {
            KalixEnv.publishProjectContainers(Seq(proj)).value
          },
          // Publishes each service to Kalix with the `latest` image tag.
          KalixEnv.deployServices    := {
            KalixEnv.deployProjectServices(Seq(proj)).value
          },
          // Publish containers + deploy services (combo command)
          KalixEnv.publishAndDeploy  := {
            KalixEnv.deployServices.dependsOn(KalixEnv.publishContainers).value
          }
        )
    }

  }

  def riddl(appName: String)(proj: Project): Project = {
    proj
      .enablePlugins(RiddlSbtPlugin)
      .settings(
        scalaVersion     := "3.3.1",
        riddlcMinVersion := "0.27.7",
        riddlcConf       := file("design/src/main/riddl/ksoapp.conf"),
        riddlcOptions    := Seq("--show-times", "--verbose"),
      )
  }

}

object Testing {

  def scalaTest(project: Project): Project = {
    project.settings(
      Test / parallelExecution := false,
      Test / testOptions += Tests.Argument("-oDF"),
      Test / logBuffered       := false,
      libraryDependencies ++= basicTestingDependencies ++ jsonDependencies
    )
  }

}

object Utils {

  import java.io.{File, FileInputStream}
  import java.security.{DigestInputStream, MessageDigest}

  def hashFile(file: File): String = {
    val buffer = new Array[Byte](8192)
    val sha1   = MessageDigest.getInstance("SHA-1")

    val dis = new DigestInputStream(new FileInputStream(file), sha1)
    try {
      while (dis.read(buffer) != -1) {}
    } finally {
      dis.close()
    }

    sha1.digest.map("%02x".format(_)).mkString
  }

}
