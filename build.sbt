name := "gatling-imap"

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.11"

val gatlingVersion = "3.9.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:postfixOps",
  "-Wconf:msg=Auto-application to \\`\\(\\)\\` is deprecated:s")

enablePlugins(GatlingPlugin)

libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion exclude("io.gatling", "gatling-http")
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion exclude("io.gatling", "gatling-http")
libraryDependencies += "com.yahoo.imapnio" % "imapnio.core" % "5.0.7"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % "test,it"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.20" % "it"
libraryDependencies += "com.typesafe.akka" %% "akka-protobuf" % "2.6.20" % "it"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.20" % "it"
libraryDependencies += "org.testcontainers" % "testcontainers" % "1.17.1" % "it"
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.1.11" % "it"


libraryDependencies += "com.github.lafa.logfast" % "logfast.core" % "1.0.6"
libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.9"
libraryDependencies += "io.netty" % "netty-handler" % "4.1.5.Final"
libraryDependencies += "com.sun.mail" % "javax.mail" % "1.5.2"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.12"
libraryDependencies += "ch.qos.logback" % "logback-core" % "1.1.3"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"

resolvers += Resolver.mavenLocal

cancelable in Global := true
