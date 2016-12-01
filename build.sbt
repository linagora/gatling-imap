name := "gatling-imap"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

enablePlugins(GatlingPlugin)

libraryDependencies += "io.gatling" % "gatling-test-framework" % "2.2.2" exclude("io.gatling", "gatling-http")
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.2" exclude("io.gatling", "gatling-http")
libraryDependencies += "com.github.krdev.imapnio" % "imapnio.core" % "1.0.21-linagora2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.11" % "test"

resolvers += Resolver.mavenLocal

cancelable in Global := true
