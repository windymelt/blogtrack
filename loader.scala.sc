#!/usr/bin/env -S scala-cli shebang -S 3
//> using scala 3.3.0
//> using dep "co.fs2::fs2-core::3.9.2"
//> using dep io.bullet::borer-core::1.11.0
//> using dep io.bullet::borer-derivation::1.11.0
//> using dep "com.github.nscala-time::nscala-time::2.32.0"
//> using dep "io.github.neotypes::neotypes-core:1.0.0-M3"
//> using dep "io.github.neotypes::neotypes-generic:1.0.0-M3"
//> using dep "org.neo4j.driver:neo4j-java-driver:5.12.0"
//> using dep "dev.kovstas::fs2-throttler:1.0.8"

import cats.effect.IOApp
import com.github.nscala_time.time.Imports._

object Config {
  val connectTo = sys.env("NEO4J_URI")
  val password = sys.env("NEO4J_PASSWORD")

  /** Put per Second for Neo4j (throttling factor)
    */
  val PPS = 5 // 5 put per second
}

case class Entry(
    url: String,
    title: String,
    tags: Seq[String],
    updatedAt: DateTime,
    cites: Set[String]
)

// TODO: use parameter
def prepareAddQuery(e: Entry): Set[String] = e.cites.map { c =>
  s"""
merge (a:Article {url:"${e.url}"})
merge (b:Article {url:"$c"})
merge (a) -[c:CITE]-> (b)
  on create set a.title = "${e.title.replaceAll(
      "\"",
      "\\\""
    )}", a.updatedAt = datetime("${e.updatedAt.toString}"), a.tags = ${e.tags
      .map(t => s"\"${t}\"")
      .mkString("[", ",", "]")}
  return a,c,b
"""
}

def prepareTagQuery(e: Entry): Set[String] = e.tags.map { t =>
  s"""merge (a:Article {url:"${e.url}"})
merge (t:Tag {title:"${t}"})
merge (a) -[e:TAGGING]-> (t)
return a,e,t
"""
}.toSet

// import neotypes.mappers.ResultMapper
// case class Node(
//     url: String,
//     title: String,
//     tags: Seq[String],
//     updatedAt: java.time.ZonedDateTime
// )
// val result = """match (a :Article) return a limit 10""".query(mapper).list(driver)
// result.map(println(_))
// Await.result(result, scala.concurrent.duration.FiniteDuration(10, "seconds"))

/** 事前にtransformer.scala.scで生成した引用辞書をNeo4jにバルクロードするツール
  *  Usage: NEO4J_URI="bolt+s://..." NEO4J_PASSWORD="***" ./loader.scala.sc fooBlog.export.txt.cites.cbor
  *  Output: n/a
  */
object LoadCitesToNeo4j extends IOApp {
  import cats.effect.ExitCode
  import cats.effect.IO
  import collection.JavaConverters._
  import dev.kovstas.fs2throttler.Throttler._
  import fs2.Stream
  import io.bullet.borer.{Cbor, Decoder}
  import io.bullet.borer.derivation.ArrayBasedCodecs._
  import java.io.File
  import neotypes.GraphDatabase
  import neotypes.generic.implicits._
  import neotypes.syntax.all._
  import org.neo4j.driver.AuthTokens
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future

  // given ResultMapper[DateTime] = ResultMapper.string.map(DateTime.parse)
  // given mapper: ResultMapper[Node] = ResultMapper.productDerive[Node]

  // CBOR decoder
  given Decoder[DateTime] =
    Decoder.forString.map[DateTime](DateTime.parse)
  given Decoder[Entry] = deriveDecoder

  val driver = GraphDatabase
    .asyncDriver[Future](Config.connectTo, AuthTokens.basic("neo4j", Config.password))

  def run(args: List[String]): IO[ExitCode] = {
    // まずCBORファイルをロードする。CBORは軽くて速いが副作用を生じるファイル操作であることにかわりはないのでIO.delayに入れておく
    val es = IO.delay {
      Cbor.decode(File(args(0))).to[Iterator[Entry]].value.toSeq
    }
    // FS2の十八番
    val entriesStream = Stream
      .evalSeq(es)// さきほどのCBORロード処理からストリームを構成する
      // prepareAddQueryは引用がない場合にSeq()を返すので、これを潰すためのイディオムとして一度Chunkに入れてflatMapする
      .map(e => fs2.Chunk.seq(prepareAddQuery(e).toSeq ++ prepareTagQuery(e).toSeq))
      .flatMap(Stream.chunk)
      // サーバにやさしくスロットリングする
      .through(throttle(Config.PPS, 1.second, Shaping))
      // 確認用に画面にクエリを流しておく
      .evalTap(q => IO.println(q))
      // evalMapでクエリを発射する。
      // Neo4Jへのリクエストは当然副作用をともなうのでIOに入れる
      // mapのかわりにevalMapするとIOを返すような操作を利用してmapした後でストリームに展開してくれる
      .evalMap(q => IO.fromFuture(IO(q.execute.resultSummary(driver))))
      // なんらかの通知があった場合は表示する
      .evalTap(s => IO.println(s.notifications().asScala.toSeq))

    // このストリームを実行したら終了する
    entriesStream.compile.drain >> IO.pure(ExitCode.Success)
  }
}

LoadCitesToNeo4j.main(args)
