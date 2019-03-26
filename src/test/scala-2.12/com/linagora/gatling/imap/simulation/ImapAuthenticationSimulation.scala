package com.linagora.gatling.imap.simulation

import com.linagora.gatling.imap.PreDef.imap
import com.linagora.gatling.imap.scenario.ImapAuthenticationScenario
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

import scala.concurrent.duration._


class ImapAuthenticationSimulation extends Simulation {

  val feeder = Array(Map("username" -> "user1", "password" -> "password")).circular
  val UserCount: Int = 10

  setUp(ImapAuthenticationScenario(feeder).inject(constantUsersPerSec(UserCount).during(2.seconds))).protocols(imap.host("localhost").build())

}
