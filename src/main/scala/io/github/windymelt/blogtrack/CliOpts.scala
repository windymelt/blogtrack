package io.github.windymelt.blogtrack

import cats.implicits.*
import com.monovore.decline.*
import scala.util.matching.Regex

case class CliOpts(
    neo4jUri: String,
    neo4jPassword: String,
    myBlogRegex: Regex,
    bearerToken: String,
)

object CliOpts {
  private def neo4juri = Opts.env[String]("NEO4J_URI", "URI for Neo4j instance")
  private def neo4jPassword =
    Opts.env[String]("NEO4J_PASSWORD", "Password for Neo4j instance")
  private def myBlogRegex = Opts.env[String](
    "MY_BLOG_REGEX",
    "Regex to distinguish url is your blog or not",
  )
  private def bearerToken =
    Opts.env[String]("BEARER_TOKEN", "Bearer Token for authentication")

  def allOpts: Opts[CliOpts] =
    (neo4juri, neo4jPassword, myBlogRegex, bearerToken).mapN(
      (uri, pw, re, token) => CliOpts(uri, pw, re.r, token)
    )
}
