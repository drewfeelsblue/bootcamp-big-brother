package http

import cats.effect.Sync
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{ ObjectMapper, PropertyNamingStrategies }
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.model.block.{ ActionsBlock, ContextBlock, InputBlock }
import com.slack.api.model.block.composition.{ MarkdownTextObject, PlainTextObject }
import com.slack.api.model.block.element.{ ButtonElement, PlainTextInputElement }
import http.Model.{ Exercise, Topic }

import java.util.List

object Responses {
  private def plainTextObject(text: String) = PlainTextObject.builder().text(text).build()

  private val objectWriter = new ObjectMapper()
    .setSerializationInclusion(Include.NON_NULL)
    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .writer()
    .withDefaultPrettyPrinter()

  def initExerciseResponse[F[_]: Sync](topic: Topic, exercise: Exercise): F[String] = Sync[F].delay {
    val response = SlashCommandResponse
      .builder()
      .responseType("in_channel")
      .blocks(
        List.of(
          InputBlock
            .builder()
            .element(
              PlainTextInputElement
                .builder()
                .placeholder(plainTextObject("Submit your code here..."))
                .multiline(true)
                .build()
            )
            .label(plainTextObject(s"${topic.value} ${exercise.value}"))
            .blockId("input")
            .build(),
          ActionsBlock
            .builder()
            .elements(
              List.of(
                ButtonElement
                  .builder()
                  .text(plainTextObject("Submit"))
                  .value("Submit")
                  .build()
              )
            )
            .blockId("actions")
            .build()
        )
      )
      .build()

    objectWriter.writeValueAsString(response)
  }

  def initExerciseErrorResponse[F[_]: Sync](command: String, args: String): F[String] = Sync[F].delay {
    val response = SlashCommandResponse
      .builder()
      .blocks(
        List.of(
          ContextBlock
            .builder()
            .blockId("context")
            .elements(
              List.of(
                MarkdownTextObject.builder().text(s"Invalid syntax: command = _${command}_, args = _${args}_").build(),
                MarkdownTextObject.builder().text("Please, use /command --help to see all possible options").build()
              )
            )
            .build()
        )
      )
      .build()

    objectWriter.writeValueAsString(response)
  }

}
