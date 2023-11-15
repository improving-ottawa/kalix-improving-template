import kalix.sbt.KalixPlugin.KalixProtocolVersion
import sbt._

object Dependencies {
  object Versions {
    val akka = "2.7.0"
    val akkaHttp = "10.4.0"
    val akkaKafka = "4.0.0"
    val alpakka = "5.0.0"
    val auth0 = "2.6.1"
    val chimney = "0.6.2"
    val commonsCodec = "1.15"
    val jsoniterScala = "2.21.3"
    val jwtScala = "9.4.4"
    val kantanCsv = "0.7.0"
    val logback = "1.4.7"
    val monocle = "3.1.0"
    val oidc4s = "0.11.0"
    val pureconfig = "0.17.4"
    val scapegoat = "2.1.0"
    val scalalogging = "3.9.5"
    val scalamock = "5.2.0"
    val scalapbCompiler = "0.11.13"
    val scalatest = "3.2.16"
    val scalacheck = "1.17.0"
    val sttp = "3.8.13"
    val testcontainers = "1.17.6"
    val cats = "2.10.0"
    val catsEffect = "3.5.2"
    val circe = "0.14.5"
    val gatling = "3.9.2"
    val commons_io = "20030203.000550"
    val commons_codec = "20041127.091804"
    val compress = "1.23.0"
    val google_grpc = "2.24.0"
    val kalixSDK = "1.3.3"
    val lang3 = "3.13.0"
    val scopt = "4.1.0"
    val slf4j = "2.0.5"

  }

  import Versions._

  lazy val akkaGrpcDepsPackage = Seq(
    "com.typesafe.akka" %% "akka-actor" % akka,
    "com.typesafe.akka" %% "akka-discovery" % akka,
    "com.typesafe.akka" %% "akka-protobuf-v3" % akka,
    "com.typesafe.akka" %% "akka-stream" % akka,
    "com.typesafe.akka" %% "akka-slf4j" % akka,
    "com.typesafe.akka" %% "akka-http" % akkaHttp,
    "com.typesafe.akka" %% "akka-parsing" % akkaHttp,
    "com.typesafe.akka" %% "akka-testkit" % akka % Test,
  )

  lazy val akkaKalixServiceDepsPackage = Seq(
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttp,
    "com.typesafe.akka" %% "akka-http2-support" % akkaHttp,
  )

  lazy val authentication = Seq(
    "com.auth0" % "auth0" % auth0,
    "me.wojnowski" %% "oidc4s-core" % oidc4s,
    "me.wojnowski" %% "oidc4s-circe" % oidc4s,
    "me.wojnowski" %% "oidc4s-sttp" % oidc4s,
  ) ++
    httpDepsPackage ++
    functionalDepsPackage

  lazy val bouncyCastleCryptoPackage = Seq(
    "org.bouncycastle" % "bcprov-jdk15on" % "1.70",
    "org.bouncycastle" % "bcpkix-jdk15on" % "1.70"
  )

  lazy val commons_io = "commons-io" % "commons-io" % Versions.commons_io
  lazy val google_grpc = "com.google.api.grpc" % "proto-google-common-protos" % Versions.google_grpc % "protobuf"

  lazy val lang3 = "org.apache.commons" % "commons-lang3" % Versions.lang3
  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig-core" % Versions.pureconfig
  lazy val scalactic = "org.scalactic" %% "scalactic" % Versions.scalatest % "test"
  lazy val scalamock = "org.scalamock" %% "scalamock" % Versions.scalamock % Test
  lazy val scalatest = "org.scalatest" %% "scalatest" % Versions.scalatest % "test"
  lazy val scalacheck = "org.scalacheck" %% "scalacheck" % Versions.scalacheck % "test"
  lazy val slf4j = "org.slf4j" % "slf4j-nop" % Versions.slf4j

  lazy val grpc: Seq[ModuleID] = Seq(
    google_grpc,
    "com.typesafe.akka" %% "akka-actor-typed" % akka,
    "com.typesafe.akka" %% "akka-discovery" % akka,
    "com.typesafe.akka" %% "akka-protobuf-v3" % akka,
    "com.typesafe.akka" %% "akka-stream" % akka,
    "com.typesafe.akka" %% "akka-slf4j" % akka,
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  )

  lazy val commons_codec = "commons-codec" % "commons-codec" % Versions.commons_codec

  lazy val kalixScalaSdk: ModuleID = "io.kalix" %% "kalix-scala-sdk-protobuf" % Versions.kalixSDK
  lazy val kalixJvmCoreSdk: ModuleID = "io.kalix" %% "kalix-jvm-core-sdk" % Versions.kalixSDK % "protobuf"

  lazy val csvParsingDepsPackage: Seq[ModuleID] =
    functionalDepsPackage ++ Seq(
      "com.nrinaudo" %% "kantan.csv" % kantanCsv,
      "com.nrinaudo" %% "kantan.csv-java8" % kantanCsv,
      "com.nrinaudo" %% "kantan.csv-cats" % kantanCsv,
      "com.nrinaudo" %% "kantan.csv-generic" % kantanCsv
    )

  lazy val jwtSupportPackage = Seq(
    "com.github.jwt-scala" %% "jwt-core" % jwtScala,
    "com.github.jwt-scala" %% "jwt-circe" % jwtScala
  )

  lazy val javaLibRecur = "org.dmfs" % "lib-recur" % "0.15.0"

  lazy val testingDeps: Seq[ModuleID] = Seq(
    scalactic,
    scalatest,
    scalacheck,
    "org.scalatestplus" %% "scalacheck-1-17" % "3.2.17.0" % "test"
  )

  val basicTestingDependencies: Seq[ModuleID] = testingDeps

  val loggingDependencies: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % logback,
    "com.typesafe.scala-logging" %% "scala-logging" % scalalogging
  )

  val utilityDependencies: Seq[ModuleID] = Seq(
    // "org.scala-lang.modules" %% "scala-xml" % scalaxml,
    // "io.scalaland" %% "chimney" % chimney,
    pureconfig,
    scalacheck
    // "deVersions.optics" %% "monocle-core" % monocle,
    // "deVersions.optics" %% "monocle-macro" % monocle,
    // noinspection SbtDependencyVersionInspection
    // "commons-codec" % "commons-codec" % commonsCodec,
    // "org.typelevel" %% "cats-core" % cats
  )

  lazy val functionalDepsPackage: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-core" % cats,
    "org.typelevel" %% "cats-effect" % catsEffect,
    "org.typelevel" %% "cats-kernel" % cats,
    "org.typelevel" %% "jawn-parser" % "1.5.1",
  )

  lazy val httpDepsPackage: Seq[ModuleID] = Seq(
    "com.softwaremill.sttp.client3" %% "core" % sttp,
    "com.softwaremill.sttp.client3" %% "jsoniter" % sttp,
    "com.softwaremill.sttp.client3" %% "circe" % sttp,
    "com.softwaremill.sttp.client3" %% "akka-http-backend" % sttp,
    "com.softwaremill.sttp.client3" %% "cats" % sttp
  )

  lazy val jsonDependencies: Seq[ModuleID] = Seq(
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterScala,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterScala,
    "io.circe" %% "circe-core" % circe,
    "io.circe" %% "circe-generic" % circe,
    "io.circe" %% "circe-parser" % circe,
    "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.0"
  )

  lazy val integrationTestDependencies: Seq[ModuleID] = Seq(
    "org.testcontainers" % "testcontainers" % testcontainers % Test,
    "com.typesafe.akka" %% "akka-testkit" % akka % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akka % Test,
    "com.typesafe.akka" %% "akka-stream" % akka % Test,
    "com.lightbend.akka" %% "akka-stream-alpakka-google-cloud-pub-sub-grpc" % alpakka % Test,
    "com.typesafe.akka" %% "akka-stream-kafka" % akkaKafka % Test,
    "com.typesafe.akka" %% "akka-http" % akkaHttp % Test,
    "com.typesafe.akka" %% "akka-http2-support" % akkaHttp % Test
  )

  val loadTestDependencies: Seq[ModuleID] = Seq(
    "io.gatling.highcharts" % "gatling-charts-highcharts" % gatling % "test,it",
    "io.gatling" % "gatling-test-framework" % gatling % "test,it",
    "io.circe" %% "circe-core" % circe % "test,it",
    "io.circe" %% "circe-generic" % circe % "test,it",
    "io.circe" %% "circe-parser" % circe % "test,it"
  )

  val scalaPbDependencies: Seq[ModuleID] = Seq(
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "com.google.protobuf" % "protobuf-java" % "3.22.2" % "protobuf",
    "io.kalix" % "kalix-sdk-protocol" % KalixProtocolVersion % "protobuf-src"
  )

  val scalaPbCompilerPlugin: ModuleID = "com.thesamet.scalapb" %% "compilerplugin" % Versions.scalapbCompiler

  val scalaPbValidationDependencies: Seq[ModuleID] = Seq(
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "com.thesamet.scalapb" %% "scalapb-validate-core" % scalapb.validate.compiler.BuildInfo.version % "protobuf",
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  )

  val scalaPbGoogleCommonProtos: Seq[ModuleID] = Seq(
    "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.9.6-0" % "protobuf",
    "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.9.6-0"
  )

}
