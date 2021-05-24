import sbt._

object Dependencies {
  object Versions {
    val catsEffect          = "2.2.0"
    val http4s              = "0.21.13"
    val pureConfig          = "0.15.0"
    val refined             = "0.9.24"
    val skunk               = "0.0.26"
    val slackMorphismClient = "3.1.0"
    val newType             = "0.4.4"
    val sttp                = "2.0.6"
    val scache              = "2.2.1"

    val postgresJdbc = "42.2.20"
    val flyway       = "7.8.2"

    val log4cats = "1.2.1"
    val logback  = "1.2.3"

    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.11.3"
  }
  object Libraries {
    def http4s(artifact: String): ModuleID        = "org.http4s" %% artifact % Versions.http4s
    def refinedModule(artifact: String): ModuleID = "eu.timepit" %% artifact % Versions.refined

    val catsEffect = "org.typelevel"                %% "cats-effect"    % Versions.catsEffect
    val pureConfig = "com.github.pureconfig"        %% "pureconfig"     % Versions.pureConfig
    val skunkCore  = "org.tpolecat"                 %% "skunk-core"     % Versions.skunk
    val skunkCirce = "org.tpolecat"                 %% "skunk-circe"    % Versions.skunk
    val newType    = "io.estatico"                  %% "newtype"        % Versions.newType
    val sttp       = "com.softwaremill.sttp.client" %% "http4s-backend" % Versions.sttp
    val scache     = "com.evolutiongaming"          %% "scache"         % Versions.scache

    val postgresJdbc = "org.postgresql" % "postgresql"  % Versions.postgresJdbc
    val flyway       = "org.flywaydb"   % "flyway-core" % Versions.flyway

    val log4cats = "org.typelevel" %% "log4cats-slf4j"  % Versions.log4cats
    val logback  = "ch.qos.logback" % "logback-classic" % Versions.logback

    val slackMorphismClient = "org.latestbit" %% "slack-morphism-client" % Versions.slackMorphismClient

    val http4sDsl    = http4s("http4s-dsl")
    val http4sServer = http4s("http4s-blaze-server")
    val http4sClient = http4s("http4s-blaze-client")
    val http4sCirce  = http4s("http4s-circe")

    val refined           = refinedModule("refined")
    val refinedPureConfig = refinedModule("refined-pureconfig")
  }

  object CompilerPlugins {
    val `kind-projector`     = "org.typelevel" %% "kind-projector"     % Versions.kindProjector cross CrossVersion.full
    val `better-monadic-for` = "com.olegpy"    %% "better-monadic-for" % Versions.betterMonadicFor
  }
}
