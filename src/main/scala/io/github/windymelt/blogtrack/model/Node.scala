package io.github.windymelt.blogtrack.model

case class Node(
    url: String,
    title: String,
    tags: Seq[String],
    updatedAt: java.time.ZonedDateTime
)
