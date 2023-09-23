import smithy4s.codegen.Smithy4sCodegenPlugin

ThisBuild / scalaVersion := "3.3.0"

val smithy4sVersion = "0.17.19"

val blogtrack = project
  .in(file("."))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s-swagger" % smithy4sVersion,
      "org.http4s" %% "http4s-ember-server" % "0.23.23",
      "com.github.nscala-time" %% "nscala-time" % "2.32.0",
      "io.github.neotypes" %% "neotypes-core" % "1.0.0-M3",
      "io.github.neotypes" %% "neotypes-generic" % "1.0.0-M3",
      "io.github.neotypes" %% "neotypes-cats-effect" % "1.0.0-M3",
      "org.typelevel" %% "cats-effect" % "3.5.1",
      "org.neo4j.driver" % "neo4j-java-driver" % "5.12.0",
    )
  )
