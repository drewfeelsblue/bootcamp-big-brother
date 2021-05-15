package resources

import cats.effect.{ ConcurrentEffect, Resource, Timer }
import config.HttpServerConfig
import org.http4s.HttpApp
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object HttpServer {
  def resource[F[_]: ConcurrentEffect: Timer](httpServerConfig: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server[F]] =
    BlazeServerBuilder[F](ExecutionContext.global)
      .bindHttp(httpServerConfig.port.value, httpServerConfig.host.value)
      .withHttpApp(httpApp)
      .resource
}
