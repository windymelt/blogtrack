package io.github.windymelt.blogtrack

import cats.effect.{IO, Resource}
import neotypes.cats.effect.implicits.*
import neotypes.syntax.all.*
import neotypes.{AsyncDriver, GraphDatabase}
import org.neo4j.driver.AuthTokens

/** Neotypesを使ってNeo4jと通信するクライアント。インターフェイスは [[cats.effect.IO]] で提供している。
  */
class Neo4jClient(url: String, password: String) {
  private lazy val driver: Resource[IO, AsyncDriver[IO]] = GraphDatabase
    .asyncDriver[IO](url, AuthTokens.basic("neo4j", password))

  /** 所与のURLのエントリを引用しているエントリを検索する。
    *
    * @param url
    *   引用されているエントリのURL。
    * @return
    *   そのURLのエントリを引用しているエントリのリスト。
    */
  def getCited(url: java.net.URL): IO[Seq[model.Node]] = {
    driver.use { neo4j =>
      s"""match (a :Article) -[c :CITE]-> (b :Article { url: "${url}" }) return a limit 50"""
        .query(model.instance.NodeCodec.mapper)
        .list(neo4j)
    }
  }
}
