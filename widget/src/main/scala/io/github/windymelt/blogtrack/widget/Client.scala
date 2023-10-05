package io.github.windymelt.blogtrack.widget

import cats.effect.{IO, Resource}
import org.http4s.{AuthScheme, Credentials, Uri, client, headers}
import smithy4s.http4s.{ClientEndpointMiddleware, SimpleRestJsonBuilder}
import io.github.windymelt.blogtrack.api.*
import org.http4s.client.Client
import smithy4s.{Endpoint, Hints, Service}

object BlogTrackClient {
  val blogTrackClient: Resource[IO, BlogTrackService[IO]] = for {
    client <- org.http4s.dom.FetchClientBuilder[IO].resource
    blogTrackClient <-SimpleRestJsonBuilder(BlogTrackService)
      .client(client)
      .uri(Uri.unsafeFromString("http://localhost:9000"))
      .middleware(AuthMiddleware.bearerAuth)
      .resource
  } yield blogTrackClient

  object AuthMiddleware {
    private def bearerAuthMiddleware(): Client[IO] => Client[IO] = { inputClient =>
      Client[IO] { request =>
        val hdrs = request.headers.put(headers.Authorization(Credentials.Token(AuthScheme.Bearer, "foobar")))
        inputClient.run(request.withHeaders(hdrs))
      }
    }
    val bearerAuth: ClientEndpointMiddleware[IO] = new ClientEndpointMiddleware.Simple[IO] {
      private val mid: Client[IO] => Client[IO] = bearerAuthMiddleware()
      override def prepareWithHints(serviceHints: Hints, endpointHints: Hints): Client[IO] => Client[IO] = {
        serviceHints.get[smithy.api.HttpBearerAuth] match
          case Some(_) =>
            endpointHints.get[smithy.api.Auth] match {
              case Some(auths) if auths.value.isEmpty => identity
              case _ => mid
            }
          case None => identity
      }
    }
  }
}
