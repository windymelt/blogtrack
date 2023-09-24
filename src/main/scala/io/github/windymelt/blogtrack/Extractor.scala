package io.github.windymelt.blogtrack

import cats.effect.IO
import net.ruippeixotog.scalascraper.browser._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model._

import scala.util.matching.Regex

/** はてなブログの記事から自分のブログ内リンクを抽出するクラス。
  *
  * @param myBlogRegex
  *   コンテンツ中のURLを自分のブログと判定するための正規表現。
  */
class Extractor(myBlogRegex: Regex) {
  private lazy val browser = JsoupBrowser()

  /** 指定したURLのコンテンツを取得し、エントリ情報と引用情報を抽出する。
    *
    * @param url
    *   自分のブログ内のエントリURL。
    * @return
    *   抽出したエントリ情報と引用URLのペア。
    */
  def extractLinks(url: String): IO[Option[(model.Node, Seq[String])]] =
    IO.delay {
      if (myBlogRegex.matches(url)) {
        val doc = browser.get(url)
        val cites = (doc >> elementList("cite a") >> attrs("href")).flatten.view
          .filter(myBlogRegex.matches)
        val title =
          doc >> element("div > header > h1.entry-title > a") >> text
        val tags = (doc >> elementList(
          "div > header > div.entry-categories.categories > a.entry-category-link"
        ) >> texts).flatten
        val updatedAtStr =
          doc >> element(
            "div > header > div.date.entry-date > a > time"
          ) >> attr(
            "datetime"
          )
        val updatedAt = java.time.ZonedDateTime.parse(updatedAtStr)

        Some(
          model.Node(
            url = url,
            title = title,
            tags = tags,
            updatedAt = updatedAt,
          ) -> cites.toSeq
        )
      } else {
        None
      }
    }
}
