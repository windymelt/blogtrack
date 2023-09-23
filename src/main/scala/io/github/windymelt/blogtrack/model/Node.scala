package io.github.windymelt.blogtrack.model

/** Neo4jが返すエントリ情報を表現するノード。
  *
  * @param url
  *   エントリのURL。
  * @param title
  *   エントリのタイトル。
  * @param tags
  *   エントリにつけられたタグ。
  * @param updatedAt
  *   エントリの更新日時。
  */
case class Node(
    url: String,
    title: String,
    tags: Seq[String],
    updatedAt: java.time.ZonedDateTime
)
