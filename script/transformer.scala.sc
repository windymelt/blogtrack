#!/usr/bin/env -S scala-cli shebang -S 3
//> using scala 3.3.0
//> using dep "co.fs2::fs2-core::3.9.2"
//> using dep "co.fs2::fs2-io::3.9.2"
//> using dep "com.github.nscala-time::nscala-time::2.32.0"
//> using dep "org.jsoup:jsoup:1.16.1"
//> using dep "net.ruippeixotog::scala-scraper::3.1.0"
//> using dep io.bullet::borer-core::1.11.0
//> using dep io.bullet::borer-derivation::1.11.0

import java.io.File

import net.ruippeixotog.scalascraper.model.Document

import scala.util.Try

import cats.effect.ExitCode
import cats.effect.IOApp
import cats.effect.IO
import fs2.text
import com.github.nscala_time.time.Imports._
import net.ruippeixotog.scalascraper.browser._
import fs2.io.file.{Files, Path}
import io.bullet.borer.{Cbor, Encoder, Decoder}

// Case classをエンコードする作法としてMapを使うかArrayを使うか選べる。
// Arrayはサイズが小さくなるが互換性にシビアになる
import io.bullet.borer.derivation.ArrayBasedCodecs._

val myBlogRegex = """https://blog\.3qe\.us/.+""".r
val myBlogAuthority = """https://blog.3qe.us/entry"""
val statusesToExtract = Seq("Publish")

val EntrySplitter = "--------\n"
val SectionSplitter = "-----\n"

case class Entry(
    url: String,
    title: String,
    tags: Seq[String],
    updatedAt: DateTime,
    cites: Set[String]
)

// using terms of MT
case class Header(
    title: String,
    basename: String,
    status: String,
    date: DateTime,
    categories: Seq[String]
)

given Encoder[DateTime] = Encoder.forString.contramap[DateTime](_.toString)

given Encoder[Entry] = deriveEncoder

val dtfmt = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss")

/** はてなブログからエクスポートしたMT形式のtxtファイルから、ブログ内リンクを抽出してCBORファイルに出力するツール
  * Usage: ./transformer.scala.sc fooBlog.export.txt
  * Output: fooBlog.export.txt.cites.cbor
  */
object HatenaMTExportToCitingList extends IOApp {
  val outFile = File(s"${args(0)}.cites.cbor")
  def run(args: List[String]) =
    Files[IO]
      .readAll(Path(args(0)))
      .through(text.utf8Decode)
      .repartition(s => fs2.Chunk.array(s.split(EntrySplitter)))
      .map(parseEntry)
      .map {
        case None        => fs2.Chunk.empty
        case Some(value) => fs2.Chunk(value)
      }
      .flatMap(fs2.Stream.chunk)
      .compile
      .to(Iterator)
      .map(es => Cbor.encode(es).to(outFile).result)
      >> IO.pure(
        ExitCode.Success
      )

  val parseEntry: String => Option[Entry] =
    _.split(SectionSplitter).toList match {
      case header :: content :: _ =>
        parseHeader(header).flatMap {
          case h if statusesToExtract.contains(h.status) =>
            Some(
              Entry(
                s"$myBlogAuthority/${h.basename}",
                h.title,
                h.categories,
                h.date,
                extractCites(content)
              )
            )
          case _ => None
        }

      case _ => None
    }

  val parseHeader: String => Option[Header] = s => {
    import cats.implicits._

    val maps: Seq[Map[String, List[String]]] =
      s.split("\n").filterNot(_.isBlank()).map { kv =>
        kv.split(": ").toList match {
          case k :: vs   => Map(k -> List(vs.mkString(": ")))
          case otherwise => Map.empty
        }
      }
    val m = maps.combineAll
    Try {
      Header(
        m.getOrElse("TITLE", Seq("■")).head,
        m("BASENAME").head,
        m("STATUS").head,
        DateTime.parse(m("DATE").head, dtfmt),
        m.getOrElse("CATEGORY", Seq())
      )
    }.toOption
  }

  val browser = JsoupBrowser()
  val extractCites: String => Set[String] = s => {
    import net.ruippeixotog.scalascraper.dsl.DSL._
    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
    import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
    import net.ruippeixotog.scalascraper.model._

    val doc = browser.parseString(s)

    val ls = (doc >> elementList("cite a") >> attrs("href")).flatten.view
      .filter(myBlogRegex.matches)

    ls.toSet
  }
}

HatenaMTExportToCitingList.main(args)
