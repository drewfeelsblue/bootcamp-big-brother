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

resolvers += Resolver.bintrayRepo("evolutiongaming", "maven")

lazy val slackScalaMorphismModels =
  ProjectRef(uri("git://github.com/drewfeelsblue/slack-morphism.git#master"), "slackMorphismModels")
lazy val slackScalaMorphismClient =
  ProjectRef(uri("git://github.com/drewfeelsblue/slack-morphism.git#master"), "slackMorphismClient")

lazy val root = project
  .in(file("."))
  .dependsOn(slackScalaMorphismModels, slackScalaMorphismClient)
  .settings(
    docker / imageNames := Seq(ImageName(name.value)),
    docker / dockerfile := {
      val artifact: File     = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"

      new Dockerfile {
        from("openjdk:15")
        add(artifact, artifactTargetPath)
        entryPoint("java", "-jar", artifactTargetPath)
      }
    },
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case _                                   => MergeStrategy.first
    },
    libraryDependencies ++= Seq(
      compilerPlugin(`kind-projector`),
      compilerPlugin(`better-monadic-for`),
      postgresJdbc,
      flyway,
      catsEffect,
      http4sDsl,
      http4sCirce,
      http4sClient,
      http4sServer,
      pureConfig,
      refined,
      refinedPureConfig,
      skunkCore,
      skunkCirce,
      newType,
      sttp,
//      scache,
//      slackMorphismClient,
      log4cats,
      logback % Runtime
    )
  )
