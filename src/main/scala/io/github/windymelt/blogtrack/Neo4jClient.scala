package io.github.windymelt.blogtrack

import cats.effect.{IO, Resource}
import neotypes.cats.effect.implicits.*
import neotypes.syntax.all.*
import neotypes.{AsyncDriver, GraphDatabase}
import org.neo4j.driver.AuthTokens
import cats.implicits._

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

  /** エントリと引用の情報をNeo4jに登録する。
    *
    * @param node
    *   エントリノード。
    * @param cites
    *   エントリが行っている引用のリスト。
    * @return
    *   [[cats.effect.IO[Unit]]]
    */
  def putCitation(node: model.Node, cites: Seq[String]): IO[Unit] = driver.use {
    neo4j =>
      cites.isEmpty match {
        case true =>
          putTags(neo4j)(node)
        case false =>
          putCites(neo4j)(node, cites) >> putTags(neo4j)(node)
      }
  } >> IO.unit

  private def putCites(
      neo4j: AsyncDriver[IO]
  )(node: model.Node, cites: Seq[String]): IO[Unit] = cites
    .map { c =>
      s"""
merge (a:Article {url:"${node.url}"})
merge (b:Article {url:"$c"})
merge (a) -[c:CITE]-> (b)
  on create set a.title = "${node.title.replaceAll(
          "\"",
          "\\\"",
        )}", a.updatedAt = datetime("${node.updatedAt.toString}"), a.tags = ${node.tags
          .map(t => s"\"${t}\"")
          .mkString("[", ",", "]")}
  return a,c,b
""".execute.resultSummary(neo4j)
    }
    .sequence
    .debug() >> IO.unit

  private def putTags(neo4j: AsyncDriver[IO])(node: model.Node): IO[Unit] =
    node.tags
      .map { t =>
        s"""merge (a:Article {url:"${node.url}"})
merge (t:Tag {title:"${t}"})
merge (a) -[e:TAGGING]-> (t)
return a,e,t
""".execute.resultSummary(neo4j)
      }
      .sequence
      .debug() >> IO.unit
}
