enablePlugins(ScalaJSPlugin)

name := "JSCube"

scalaVersion := "2.11.6"
scalaJSStage in Global := FastOptStage

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.8.0"
libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.8.0"
