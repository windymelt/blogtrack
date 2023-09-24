package io.github.windymelt.blogtrack

import cats.effect.IO
import org.http4s.HttpApp
import smithy4s.http4s.ServerEndpointMiddleware
import org.http4s.*
import org.http4s.implicits.*
import smithy4s.Hints

/** Bearer tokenによる認証に使うトークン。
  *
  * @param value
  */
case class ApiToken(value: String)

trait BearerAuthChecker {
  def isAuthorized(token: ApiToken): IO[Boolean]
}

/** ミドルウェアが利用する認証機構の実装。
  */
object EnvVarBearerAuthChecker extends BearerAuthChecker {
  def isAuthorized(token: ApiToken): IO[Boolean] = {
    IO.pure(
      token.value == sys.env("BEARER_TOKEN")
    )
  }
}

object AuthMiddleware {
  private def bearerAuthMiddleware(
      authChecker: BearerAuthChecker
  ): HttpApp[IO] => HttpApp[IO] = { inputApp =>
    HttpApp[IO] { request =>
      val maybeKey = request.headers
        .get[headers.Authorization]
        .collect {
          case headers.Authorization(
                Credentials.Token(AuthScheme.Bearer, value)
              ) =>
            value
        }
        .map {
          ApiToken.apply
        }

      val isAuthorized = maybeKey
        .map { key =>
          authChecker.isAuthorized(key)
        }
        .getOrElse(IO.pure(false))

      isAuthorized.ifM(
        ifTrue = inputApp(request),
        ifFalse = IO.raiseError(
          new api.NotAuthorizedError("Not authorized! You need Bearer Token.")
        ),
      )
    }
  }
  def apply(
      authChecker: BearerAuthChecker
  ): ServerEndpointMiddleware[IO] =
    new ServerEndpointMiddleware.Simple[IO] {
      private val mid: HttpApp[IO] => HttpApp[IO] = bearerAuthMiddleware(
        authChecker
      )
      def prepareWithHints(
          serviceHints: Hints,
          endpointHints: Hints,
      ): HttpApp[IO] => HttpApp[IO] = {
        // If service requires auth, do auth.
        // If endpoint doesn't require auth, avoid auth.
        serviceHints.get[smithy.api.HttpBearerAuth] match {
          case Some(_) =>
            endpointHints.get[smithy.api.Auth] match {
              case Some(auths) if auths.value.isEmpty => identity
              case _                                  => mid
            }
          case None => identity
        }
      }
    }
}
