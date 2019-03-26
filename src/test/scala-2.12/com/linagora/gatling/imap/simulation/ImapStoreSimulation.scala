package com.linagora.gatling.imap.simulation

import com.linagora.gatling.imap.PreDef._
import com.linagora.gatling.imap.scenario.ImapStoreScenario
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

import scala.concurrent.duration._

class ImapStoreSimulation extends Simulation {
  val userFeeder = csv("data/users.csv").circular

  val scn = scenario("ImapStore")
    .feed(userFeeder)
    .exec(ImapStoreScenario.storeScenario)

  setUp(scn.inject(atOnceUsers(1))).protocols(imap.host("localhost"))
}
