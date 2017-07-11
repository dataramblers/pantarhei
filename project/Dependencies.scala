import sbt._

object Dependencies {
  lazy val akkaActor: ModuleID = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  lazy val scopt: ModuleID = "com.github.scopt" %% "scopt" % "3.6.0"
  lazy val akkaSlf4j: ModuleID = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  lazy val kafka: ModuleID = "org.apache.kafka" % "kafka-clients" % "0.11.0.0"
  lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.1"
  lazy val akkaTestkit: ModuleID = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  lazy val logbackClassic: ModuleID = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val akkaVersion = "2.5.3"
}
