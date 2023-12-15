addSbtPlugin("io.kalix"         % "sbt-kalix"             % "1.3.5")
addSbtPlugin("io.gatling"       % "gatling-sbt"           % "4.5.0")
addSbtPlugin("com.github.sbt"   % "sbt-native-packager"   % "1.9.16")
addSbtPlugin("org.scalastyle"  %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.github.sbt"   % "sbt-dynver"            % "5.0.1")
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"          % "2.5.2")
addSbtPlugin("com.github.sbt"   % "sbt-git"               % "2.0.1")
addSbtPlugin("com.thesamet"     % "sbt-protoc"            % "1.0.6")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"         % "2.0.9")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"         % "1.3.11")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"         % "0.11.0")
addSbtPlugin("com.github.sbt"   % "sbt-release"           % "1.1.0")
addSbtPlugin("org.scalastyle"  %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"           % "0.5.0")
addSbtPlugin("com.reactific"   %% "sbt-riddl"             % "0.27.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-site"              % "1.4.1")
addDependencyTreePlugin

// For ScalaPB 0.11.x we need these items
libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin"           % "0.11.11",
  "com.thesamet.scalapb" %% "scalapb-validate-codegen" % "0.3.4",
)

// This gets rid of some resolution failures by forcing the scala-xml to use
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
