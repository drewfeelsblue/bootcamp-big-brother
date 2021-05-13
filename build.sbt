import Dependencies.Libraries._
import Dependencies.CompilerPlugins._

name := "bootcamp-big-brother"

version := "0.1"

scalaVersion := "2.13.5"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Ymacro-annotations"
)

enablePlugins(DockerPlugin)

docker / dockerfile := {
  val artifact: File     = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:15")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

libraryDependencies ++= Seq(
  compilerPlugin(`kind-projector`),
  compilerPlugin(`better-monadic-for`),
  catsEffect,
  http4sDsl,
  http4sCirce,
  http4sClient,
  http4sServer,
  pureConfig,
  refined,
  refinedPureConfig,
  skunkCore,
  skunkCirce
)
