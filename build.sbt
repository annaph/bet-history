enablePlugins(JavaAppPackaging)

name := "bet-history"
organization := "org.bet.history"
version := "1.0"

scalaVersion := "2.13.4"

scalacOptions ++= Seq(
  "-encoding", "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions")

libraryDependencies ++= Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % "2.4.0",
  "org.springframework.boot" % "spring-boot-starter-jetty" % "2.4.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.3",
  "com.typesafe.akka" %% "akka-actor" % "2.6.10",
  "com.typesafe.akka" %% "akka-stream" % "2.6.10",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.scalatest" %% "scalatest" % "3.2.3" % Test,
  "org.mockito" %% "mockito-scala" % "1.16.3" % Test)

fork := true

maintainer := "anna.philips@mail.com"
dockerExposedPorts += 8080
