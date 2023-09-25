import smithy4s.codegen.Smithy4sCodegenPlugin

ThisBuild / scalaVersion := "3.3.0"

val smithy4sVersion = "0.17.19"

lazy val blogtrack = project
  .in(file("."))
  .dependsOn(protocol)
  .enablePlugins(PackPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    version := "0.0.1",
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

lazy val widget = project.in(file("widget")).dependsOn(protocol)

lazy val protocol = project
  .in(file("protocol"))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(
    libraryDependencies += "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion
  )
