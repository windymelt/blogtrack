addSbtPlugin(
  "com.disneystreaming.smithy4s" % "smithy4s-sbt-codegen" % "0.17.19"
)

addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

// for sbt-0.13.x, sbt-1.x
addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.17")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.14.0")

addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.9.0")
