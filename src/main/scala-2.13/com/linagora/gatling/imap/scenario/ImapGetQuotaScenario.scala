package com.linagora.gatling.imap.scenario

import com.linagora.gatling.imap.PreDef._
import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder

object ImapGetQuotaScenario {
  def apply(feeder: FeederBuilder): ScenarioBuilder =
    scenario("Imap")
      .feed(feeder)
      .exec(imap("Connect").connect()).exitHereIfFailed
      .exec(imap("login").login("#{username}", "#{password}").check(ok))
      .exec(imap("select").select("INBOX").check(ok))
      .exec(imap("setQuota").setQuota("#private&#{username} (STORAGE 512)").check(no)) // user does not have admin rights
      .exec(imap("getQuota").getQuota("#private&#{username}").check(ok))
}
