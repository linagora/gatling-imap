package com.linagora.gatling.imap.scenario

import com.linagora.gatling.imap.PreDef._
import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder

object ImapDeleteFolderScenario {
  def apply(feeder: FeederBuilder): ScenarioBuilder =
    scenario("Imap")
      .feed(feeder)
      .exec(imap("Connect").connect()).exitHereIfFailed
      .exec(imap("login").login("#{username}", "#{password}").check(ok))
      .exec(imap("createFolder").createFolder("Another INBOX").check(ok))
      .exec(imap("deleteFolder").deleteFolder("Another INBOX").check(ok))
}
