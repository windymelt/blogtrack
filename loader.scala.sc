#!/usr/bin/env -S scala-cli shebang -S 3
//> using scala 3.3.0
//> using dep io.bullet::borer-core::1.11.0
//> using dep io.bullet::borer-derivation::1.11.0
//> using dep "com.github.nscala-time::nscala-time::2.32.0"
//> using dep "io.github.neotypes::neotypes-core:1.0.0-M3"
//> using dep "io.github.neotypes::neotypes-generic:1.0.0-M3"
//> using dep "org.neo4j.driver:neo4j-java-driver:5.12.0"

import scala.concurrent.Future
import java.io.File
import com.github.nscala_time.time.Imports._
import io.bullet.borer.{Cbor, Encoder, Decoder}
import io.bullet.borer.derivation.ArrayBasedCodecs._
import scala.concurrent.Await

import io.bullet.borer.Cbor

case class Entry(
    url: String,
    title: String,
    tags: Seq[String],
    updatedAt: DateTime,
    cites: Set[String]
)

given Decoder[DateTime] =
  Decoder.forString.map[DateTime](DateTime.parse)

given Decoder[Entry] = deriveDecoder

val got = Cbor.decode(File(args(0))).to[Iterator[Entry]].value
println(got.take(10).toList)

val connectTo = sys.env("NEO4J_URI")
val password = sys.env("NEO4J_PASSWORD")

import neotypes.GraphDatabase
import neotypes.generic.implicits._
import neotypes.syntax.all._
import neotypes.mappers.ResultMapper
import org.neo4j.driver.AuthTokens
import scala.concurrent.ExecutionContext.Implicits.global

case class Node(
    url: String,
    title: String,
    tags: Seq[String],
    updatedAt: java.time.ZonedDateTime,
)
val driver = GraphDatabase.asyncDriver[Future](connectTo, AuthTokens.basic("neo4j", password))

// given ResultMapper[DateTime] = ResultMapper.string.map(DateTime.parse)
given mapper: ResultMapper[Node] = ResultMapper.productDerive[Node]
val result = """match (a :Article) return a limit 10""".query(mapper).list(driver)
result.map(println(_))
Await.result(result, scala.concurrent.duration.FiniteDuration(10, "seconds"))
