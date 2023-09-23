package io.github.windymelt.blogtrack

import cats.effect.{IO, Resource}
import neotypes.{AsyncDriver, GraphDatabase}
import org.neo4j.driver.AuthTokens
import neotypes.cats.effect.implicits._
import neotypes.syntax.all._

import scala.concurrent.Future

class Neo4jClient(url: String, password: String) {
  lazy val driver: Resource[IO, AsyncDriver[IO]] = GraphDatabase
    .asyncDriver[IO](url, AuthTokens.basic("neo4j", password))

  def getCited(url: java.net.URL): IO[Seq[model.Node]] = {
    driver.use { neo4j =>
      s"""match (a :Article) -[c :CITE]-> (b :Article { url: "${url}" }) return a limit 50"""
        .query(model.instance.NodeCodec.mapper)
        .list(neo4j)
    }
  }
}
