import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.annotat",
      scalaVersion := "2.12.2",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "pantarhei",
    libraryDependencies ++= Seq(
      akkaActor,
      scopt,
      akkaSlf4j,
      kafka,
      logbackClassic,
      scalaTest % Test,
      akkaTestkit % Test
    ))
