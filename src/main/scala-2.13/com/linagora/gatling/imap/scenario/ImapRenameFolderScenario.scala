package com.linagora.gatling.imap.scenario

import com.linagora.gatling.imap.PreDef._
import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder

object ImapRenameFolderScenario {
  def apply(feeder: FeederBuilder): ScenarioBuilder =
    scenario("Imap")
      .feed(feeder)
      .exec(imap("Connect").connect()).exitHereIfFailed
      .exec(imap("login").login("#{username}", "#{password}").check(ok))
      .exec(imap("createFolder").createFolder("Old folder").check(ok))
      .exec(imap("renameFolder").renameFolder("Old folder", "New folder").check(ok))
}
