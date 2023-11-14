import org.scalajs.linker.interface.{OutputPatterns, Report}
import smithy4s.codegen.Smithy4sCodegenPlugin

val ScalaVersion = "3.3.1"

val smithy4sVersion = "0.17.19"
val http4sVersion = "0.23.23"

lazy val blogtrack = projectMatrix
  .in(file("."))
  .defaultAxes(defaults*)
  .dependsOn(protocol)
  .jvmPlatform(Seq(ScalaVersion))
  .enablePlugins(PackPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    scalaVersion := ScalaVersion,
    version := "0.0.1",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s-swagger" % smithy4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "com.github.nscala-time" %% "nscala-time" % "2.32.0",
      "io.github.neotypes" %% "neotypes-core" % "1.0.0-M3",
      "io.github.neotypes" %% "neotypes-generic" % "1.0.0-M3",
      "io.github.neotypes" %% "neotypes-cats-effect" % "1.0.0-M3",
      "org.typelevel" %% "cats-effect" % "3.5.1",
      "org.neo4j.driver" % "neo4j-java-driver" % "5.12.0",
      "org.jsoup" % "jsoup" % "1.16.1",
      "net.ruippeixotog" %% "scala-scraper" % "3.1.0",
      "com.monovore" %% "decline" % "2.4.1",
      "com.monovore" %% "decline-effect" % "2.4.1",
    ),
    Compile / doc / scalacOptions ++= Seq(
      "-social-links:github::https://github.com/windymelt/blogtrack",
      "-siteroot",
      "docs",
    ),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "buildinfo",
  )

lazy val defaults =
  Seq(VirtualAxis.scalaABIVersion(ScalaVersion), VirtualAxis.jvm)

lazy val protocol = projectMatrix
  .in(file("protocol"))
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(ScalaVersion))
  .jsPlatform(Seq(ScalaVersion))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(
    scalaVersion := ScalaVersion,
    Compile / doc / sources := Seq.empty,
    libraryDependencies += "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion,
    libraryDependencies += "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion
  )

import org.scalajs.linker.interface.ModuleSplitStyle

lazy val widget = projectMatrix
  .in(file("widget"))
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .jsPlatform(Seq(ScalaVersion))
  .defaultAxes(defaults*)
  .dependsOn(protocol)
  .enablePlugins(ScalaJSPlugin) // Enable the Scala.js plugin in this project
  .settings(
    scalaVersion := ScalaVersion,

    // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,

    /* Configure Scala.js to emit modules in the optimal way to
     * connect to Vite's incremental reload.
     * - emit ECMAScript modules
     * - emit as many small modules as possible for classes in the "livechart" package
     * - emit as few (large) modules as possible for all other classes
     *   (in particular, for the standard library)
     */
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.CommonJSModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("widget")))
        .withOutputPatterns(OutputPatterns.fromJSFile("%s.js"))
    },
    externalNpm := baseDirectory.value / "../../../widget",

    /* Depend on the scalajs-dom library.
     * It provides static types for the browser DOM APIs.
     */
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0",
      "com.raquo" %%% "laminar" % "15.0.1",
      "org.typelevel" %%% "cats-effect" % "3.5.1",
      "com.disneystreaming.smithy4s" %%% "smithy4s-core" % smithy4sVersion,
      "com.disneystreaming.smithy4s" %%% "smithy4s-http4s" % smithy4sVersion,
      "org.http4s" %%% "http4s-dom" % "0.2.9",
      "org.http4s" %%% "http4s-client" % http4sVersion,
    )
  )
