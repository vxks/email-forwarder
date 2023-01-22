ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.1"

import Dependencies._

resolvers ++= Seq("public", "snapshots", "releases").map(Resolver.sonatypeRepo)

lazy val root = (project in file("."))
  .settings(
    name                := "email-forwarder",
    libraryDependencies := rootDependencies
  )
