package io.github.windymelt.blogtrack

import cats.effect.*
import cats.implicits.*
import com.comcast.ip4s.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import io.github.windymelt.blogtrack.api.*
import org.http4s.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import smithy4s.http4s.SimpleRestJsonBuilder
import org.http4s.server.middleware.CORS

/** Smithyで定義したサービス定義 [[api.BlogTrackService]] を実際に実装するオブジェクト。
  */
object BlogTrackImpl extends BlogTrackService[IO] {
  private lazy val client =
    Neo4jClient(
      sys.env("NEO4J_URI"),
      sys.env("NEO4J_PASSWORD"),
    ) // TODO: use opt value
  private lazy val extractor = Extractor(sys.env("MY_BLOG_REGEX").r)

  override def notifyNewEntry(entryUrl: Url): IO[NotifyNewEntryOutput] = for {
    pairOpt <- extractor.extractLinks(entryUrl.toString)
    _ <- pairOpt.traverse { (node, cites) => client.putCitation(node, cites) }
  } yield NotifyNewEntryOutput()

  override def readCite(citedUrl: Url): IO[ReadCiteOutput] =
    for {
      got <- client.getCited(java.net.URL(citedUrl.toString))
    } yield ReadCiteOutput(
      CitationData(
        citedUrl,
        got
          .map(n =>
            Article(title = n.title, url = Url(n.url), tags = n.tags.toList)
          )
          .toList,
      )
    )
}

/** ルーティング定義。
  */
object Routes {
  private val blogTracker: Resource[IO, HttpRoutes[IO]] =
    SimpleRestJsonBuilder
      .routes(BlogTrackImpl)
      .middleware(AuthMiddleware(EnvVarBearerAuthChecker))
      .resource
  private val docs: HttpRoutes[IO] =
    smithy4s.http4s.swagger.docs[IO](BlogTrackService)

  val all: Resource[IO, HttpRoutes[IO]] = blogTracker.map(_ <+> docs)
}

object Main
    extends CommandIOApp(
      name = "blogtrack",
      header = "Track trackback inside your hatenablog",
      version = buildinfo.BuildInfo.version,
    ) {
  override def main: Opts[IO[ExitCode]] = CliOpts.allOpts.map { opts =>
    Routes.all
      .flatMap { routes =>
        EmberServerBuilder
          .default[IO]
          .withPort(port"9000")
          .withHost(host"0.0.0.0")
          .withHttpApp(CORS(routes.orNotFound))
          .withShutdownTimeout(
            scala.concurrent.duration.FiniteDuration(1, "second")
          )
          .build
      }
      .use(_ => IO.never) >> IO.pure(ExitCode.Success)
  }
}
