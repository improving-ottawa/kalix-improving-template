
val toolkitV = "q"
val toolkit = "org.scala-lang" %% "toolkit" % toolkitV
val toolkitTest = "org.scala-lang" %% "toolkit-test" % toolkitV

ThisBuild / scalaVersion := "2.13.10"
libraryDependencies += toolkit
libraryDependencies += (toolkitTest % Test)
