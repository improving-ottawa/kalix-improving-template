import sbt._

/**
  * [[CompilerPlugins]] shared/used by all Scala modules.
  * @note
  *   Defined here so that each module's meta-build project can import this file into the build source. In other words,
  *   this cannot be simply included in the plugin which gets attached to each module.
  */
object CompilerPlugins {

  // Compiler Plugin Versions
  object V {
    val betterForComp = "0.3.1"
    val kindProjector = "0.13.2"
    val scalaProtobuf = "0.11.9"
  }

  // Compiler Plugins
  val betterForComp  = "com.olegpy"           %% "better-monadic-for" % V.betterForComp
  val kindProjector  = ("org.typelevel"       %% "kind-projector"     % V.kindProjector).cross(CrossVersion.full)
  val scalaPBRuntime = "com.thesamet.scalapb" %% "scalapb-runtime"    % V.scalaProtobuf % "protobuf"

}
