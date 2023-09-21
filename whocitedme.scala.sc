#!/usr/bin/env -S scala-cli shebang -S 3
//> using scala 3.3.0
//> using dep "com.github.nscala-time::nscala-time::2.32.0"
//> using dep "io.github.neotypes::neotypes-core:1.0.0-M3"
//> using dep "io.github.neotypes::neotypes-generic:1.0.0-M3"
//> using dep "org.neo4j.driver:neo4j-java-driver:5.12.0"
//> using dep "com.github.nscala-time::nscala-time::2.32.0"

/** あるURLを引用している記事を探すスクリプト
  * Usage: NEO4J_URI="bolt+s://..." NEO4J_PASSWORD="***" ./whocitedme.scala.sc https://blog.3qe.us/entry/2023/01/07/211417
  */

import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import neotypes.mappers.ResultMapper
import neotypes.GraphDatabase
import neotypes.generic.implicits._
import neotypes.syntax.all._
import org.neo4j.driver.AuthTokens
import com.github.nscala_time.time.Imports._

object Config {
  val connectTo = sys.env("NEO4J_URI")
  val password = sys.env("NEO4J_PASSWORD")
}

case class Node(
    url: String,
    title: String,
    tags: Seq[String],
    updatedAt: java.time.ZonedDateTime
)
given ResultMapper[DateTime] = ResultMapper.string.map(DateTime.parse)
given mapper: ResultMapper[Node] = ResultMapper.productDerive[Node]

val driver = GraphDatabase
    .asyncDriver[Future](Config.connectTo, AuthTokens.basic("neo4j", Config.password))

val cited = args(0)
val result = s"""match (a :Article) -[c :CITE]-> (b :Article { url: "${cited}" }) return a limit 50"""
  .query(mapper)
  .list(driver)

val waitedResult = Await.result(result, scala.concurrent.duration.FiniteDuration(10, "seconds"))
println(waitedResult.map(_.url).mkString("\n"))
