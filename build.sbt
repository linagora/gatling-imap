name := "gatling-imap"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.8"

val gatlingVersion = "3.0.3"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature", "-language:postfixOps")

enablePlugins(GatlingPlugin)

libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion exclude("io.gatling", "gatling-http")
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion exclude("io.gatling", "gatling-http")
libraryDependencies += "com.github.krdev.imapnio" % "imapnio.core" % "1.0.22"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test,it"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.21" % "it"
libraryDependencies += "org.testcontainers" % "testcontainers" % "1.11.0" % "it"

resolvers += Resolver.mavenLocal

cancelable in Global := true
