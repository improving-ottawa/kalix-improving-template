import sbt.Keys.*
import sbt.*
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoObject, buildInfoPackage, buildInfoUsePackageAsPath}
import sbtbuildinfo.BuildInfoOption.{BuildTime, ToJson, ToMap}
import sbtbuildinfo.BuildInfoPlugin.autoImport.buildInfoOptions
import scoverage.ScoverageKeys.*

import java.util.Calendar

object C {

  def withBuildInfo(
                     homePage: String,
                     orgName: String,
                     packageName: String,
                     objName: String = "BuildInfo",
                     baseYear: Int = 2023
                   )(p: Project): Project = {
    p.settings(
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
          "copyright" -> s"© ${v.map(_.toString).getOrElse(baseYear.toString)}-${
            Calendar
              .getInstance()
              .get(Calendar.YEAR)
          } $orgName}"
        },
        scalaVersion,
        sbtVersion,
        BuildInfoKey.map(scalaVersion) { case (k, v) =>
          val version = if (v.head == '2') {
            v.substring(0, v.lastIndexOf('.'))
          }
          else v
          "scalaCompatVersion" -> version
        },
        BuildInfoKey.map(licenses) { case (k, v) =>
          k -> v.map(_._1).mkString(", ")
        }
      )
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

  lazy val scala_2_options: Seq[String] =
    Seq(
      "-deprecation",
      "-feature",
      "-new-syntax",
      // "-explain",
      // "-explain-types",
      "-Werror",
      "-pagewidth",
      "120"
    )

  def withScala3(p: Project): Project = {
    p.settings(
      scalaVersion := "2.13.10",
      scalacOptions := scala_2_options,
    )
  }
}
