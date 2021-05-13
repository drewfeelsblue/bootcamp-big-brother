import sbt._

object Dependencies {
  object Versions {
    val catsEffect = "2.2.0"
    val http4s     = "0.21.13"
    val pureConfig = "0.15.0"
    val refined    = "0.9.24"

    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.11.3"
  }
  object Libraries {
    def http4s(artifact: String): ModuleID        = "org.http4s" %% artifact % Versions.http4s
    def refinedModule(artifact: String): ModuleID = "eu.timepit" %% artifact % Versions.refined

    val catsEffect = "org.typelevel"         %% "cats-effect" % Versions.catsEffect
    val pureConfig = "com.github.pureconfig" %% "pureconfig"  % Versions.pureConfig

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
