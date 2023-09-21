import kalix.sbt.KalixPlugin.KalixProtocolVersion
import sbt.*

/** V - Dependency Versions object */
object V {
  val circe = "0.14.5"
  val commons_io = "20030203.000550"
  val commons_codec = "20041127.091804"
  val compress = "1.23.0"
  val google_grpc = "2.24.0"
  val kalixSDK = "1.3.2"
  val jsoniterScala = "2.21.3"
  val lang3 = "3.13.0"
  val logback = "1.4.5"
  val pureconfig = "0.17.4"
  val scalapbCompiler = "0.11.13"
  val scalacheck = "1.17.0"
  val scalalogging = "3.9.5"
  val scalamock = "5.2.0"
  val scalatest = "3.2.16"
  val scopt = "4.1.0"
  val slf4j = "2.0.4"
}

object Dep {
  lazy val commons_io = "commons-io" % "commons-io" % V.commons_io
  lazy val google_grpc = "com.google.api.grpc" % "proto-google-common-protos" % V.google_grpc % "protobuf"

  lazy val lang3 = "org.apache.commons" % "commons-lang3" % V.lang3
  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig-core" % V.pureconfig
  lazy val scalactic = "org.scalactic" %% "scalactic" % V.scalatest % "test"
  lazy val scalamock = "org.scalamock" %% "scalamock" % V.scalamock % Test
  lazy val scalatest = "org.scalatest" %% "scalatest" % V.scalatest % "test"
  lazy val scalacheck = "org.scalacheck" %% "scalacheck" % V.scalacheck % "test"
  lazy val slf4j = "org.slf4j" % "slf4j-nop" % V.slf4j

  lazy val grpc: Seq[ModuleID] = Seq(
    google_grpc,
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  )
  lazy val commons_codec = "commons-codec" % "commons-codec" % V.commons_codec

  lazy val testing: Seq[ModuleID] = Seq(scalactic, scalatest, scalacheck)

  lazy val kalixScalaSdkProtobuf: ModuleID = "io.kalix" %% "kalix-scala-sdk-protobuf" % V.kalixSDK
  lazy val kalixJvmCoreSdk: ModuleID = "io.kalix" %% "kalix-jvm-core-sdk" % V.kalixSDK % "protobuf"

  lazy val kalixProto: Seq[ModuleID] = Seq(kalixScalaSdkProtobuf, kalixJvmCoreSdk)

  lazy val scalaPbDependencies: Seq[ModuleID] = Seq(
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "io.kalix" % "kalix-sdk-protocol" % KalixProtocolVersion % "protobuf-src",
    "com.google.protobuf" % "protobuf-java" % "3.22.2" % "protobuf"
  )

  val scalaPbCompilerPlugin: ModuleID = "com.thesamet.scalapb" %% "compilerplugin" % V.scalapbCompiler

  val scalaPbValidateCore: ModuleID =
    "com.thesamet.scalapb" %% "scalapb-validate-core" % scalapb.validate.compiler.BuildInfo.version % "protobuf"

  val loggingDependencies: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % V.logback,
    "com.typesafe.scala-logging" %% "scala-logging" % V.scalalogging
  )

  val basicTestingDependencies: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % V.scalatest % Test,
    "org.scalamock" %% "scalamock" % V.scalamock % Test,
    "org.scalacheck" %% "scalacheck" % V.scalacheck % "test"
  )

  val jsonDependencies: Seq[ModuleID] = Seq(
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % V.jsoniterScala,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % V.jsoniterScala,
    "io.circe" %% "circe-core" % V.circe,
    "io.circe" %% "circe-generic" % V.circe,
    "io.circe" %% "circe-parser" % V.circe
  )

}
