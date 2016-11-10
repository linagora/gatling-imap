package com.linagora.gatling.imap.scenario

import com.linagora.gatling.imap.PreDef._
import io.gatling.app.Gatling
import io.gatling.core.Predef._
import io.gatling.core.config.GatlingPropertiesBuilder
import io.gatling.core.scenario.Simulation

import scala.concurrent.duration._

class ImapAuthenticationScenario extends Simulation {
  val feeder = Array(Map("username" -> "user1", "password" -> "password")).circular
  val UserCount: Int = 10

  val scn = scenario("ImapAuthentication").feed(feeder)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .exec(imap("select").select("INBOX").check(ok, hasRecent(0)))

  setUp(scn.inject(constantUsersPerSec(UserCount).during(2.seconds))).protocols(imap.host("localhost"))
}

object Engine extends App {
  // This sets the class for the simulation we want to run.
  val simClass = classOf[ImapAuthenticationScenario].getName

  val props = new GatlingPropertiesBuilder
  props.sourcesDirectory("./src/main/scala")
  props.binariesDirectory("./target/scala-2.11/classes")
  props.simulationClass(simClass)

  Gatling.fromMap(props.build)

}
