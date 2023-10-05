package io.github.windymelt.blogtrack.widget

import cats.effect.{IO, Resource}
import org.http4s.client
import smithy4s.http4s.{ClientEndpointMiddleware, SimpleRestJsonBuilder}
import io.github.windymelt.blogtrack.api.*
import org.http4s.Uri
import org.http4s.client.Client
import smithy4s.Hints

object Client {
  val blogTrackClient: Resource[IO, BlogTrackService[IO]] = for {
    client <- org.http4s.dom.FetchClientBuilder[IO].resource
    blogTrackClient <-SimpleRestJsonBuilder(BlogTrackService)
      .client(client)
      .uri(Uri.unsafeFromString("http://localhost:9000"))
      .middleware(AuthMiddleware.bearerAuth)
      .resource
  } yield blogTrackClient

  object AuthMiddleware {
    val bearerAuth: ClientEndpointMiddleware.Simple[IO] = new ClientEndpointMiddleware.Simple[IO] {
      override def prepareWithHints(serviceHints: Hints, endpointHints: Hints): Client[IO] => Client[IO] = identity
    }
  }
}
