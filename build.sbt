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
      "org.jsoup" % "jsoup" % "1.16.1",
      "net.ruippeixotog" %% "scala-scraper" % "3.1.0",
      "com.monovore" %% "decline" % "2.4.1",
      "com.monovore" %% "decline-effect" % "2.4.1"
    ),
    Compile / doc / scalacOptions ++= Seq(
      "-social-links:github::https://github.com/windymelt/blogtrack",
      "-siteroot",
      "docs"
    )
  )
