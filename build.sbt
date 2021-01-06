name := "gatling-imap"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.8"

val gatlingVersion = "3.0.3"
val playWsVersion = "2.0.1"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature", "-language:postfixOps")

enablePlugins(GatlingPlugin)

libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion exclude("io.gatling", "gatling-http")
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion exclude("io.gatling", "gatling-http")
libraryDependencies += "com.yahoo.imapnio" % "imapnio.core" % "4.3.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test,it"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.21" % "it"
libraryDependencies += "org.testcontainers" % "testcontainers" % "1.11.0" % "it"
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % playWsVersion % "it"


libraryDependencies += "com.github.lafa.logfast" % "logfast.core" % "1.0.6"
libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.9"
libraryDependencies += "io.netty" % "netty-handler" % "4.1.5.Final"
libraryDependencies += "com.sun.mail" % "javax.mail" % "1.5.2"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.12"
libraryDependencies += "ch.qos.logback" % "logback-core" % "1.1.3"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"

resolvers += Resolver.mavenLocal
resolvers += "Yahoo" at "https://dl.bintray.com/yahoo/maven"

cancelable in Global := true
