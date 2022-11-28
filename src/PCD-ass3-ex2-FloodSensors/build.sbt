ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"
val AkkaVersion = "2.7.0"

lazy val root = (project in file("."))
  .settings(
    name := "PCD-ass3-ex2-FloodSensors",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.4.5"
    )
  )
