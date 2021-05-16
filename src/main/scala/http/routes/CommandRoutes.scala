package http.routes

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits.toBifunctorOps
import cats.{ Applicative, Defer, Monad, MonadError }
import cats.syntax.functor._
import cats.syntax.flatMap._
import com.slack.api.app_backend.slash_commands.SlashCommandPayloadParser
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload
import http.Model.{ Exercise, Topic }
import http.Responses
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`

import scala.util.Try

sealed trait SlashCommandOptions
object SlashCommandOptions {
  final case class Init(topic: Topic, exercise: Exercise) extends SlashCommandOptions
  final case class InvalidSyntax(command: String, args: String) extends SlashCommandOptions

  def of(slashCommandPayload: SlashCommandPayload): SlashCommandOptions = {
    val text = Option(slashCommandPayload.getText).getOrElse("")
    text.split(" ").toList match {
      case topic :: exercise :: Nil => Init(Topic(topic), Exercise(exercise))
      case _                        => InvalidSyntax(slashCommandPayload.getCommand, slashCommandPayload.getText)
    }
  }
}

final case class CommandRoutes[F[_]: Monad: Defer: Sync]()(implicit me: MonadError[F, Throwable]) extends Http4sDsl[F] {
  import SlashCommandOptions._
  private val parser = new SlashCommandPayloadParser()

  //TODO Either.catchNonFatal
  implicit def slashCommandPayloadDecoder[F[_]: Sync: Applicative]: EntityDecoder[F, SlashCommandPayload] =
    EntityDecoder.text[F].flatMapR[SlashCommandPayload] { s =>
      EitherT.fromEither(Try(parser.parse(s)).toEither.leftMap { th =>
        InvalidMessageBodyFailure("Invalid payload", Some(th))
      })
    }

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "command" =>
      for {
        payload <- req.as[SlashCommandPayload]
        res <- SlashCommandOptions.of(payload) match {
                case Init(topic, exercise) =>
                  Responses
                    .initExerciseResponse(topic, exercise)
                    .flatMap(r => Ok.apply(r, `Content-Type`(MediaType.application.json)))
                case InvalidSyntax(command, args) =>
                  Responses
                    .initExerciseErrorResponse(command, args)
                    .flatMap(r => Ok.apply(r, `Content-Type`(MediaType.application.json)))
              }
      } yield res
  }
}
