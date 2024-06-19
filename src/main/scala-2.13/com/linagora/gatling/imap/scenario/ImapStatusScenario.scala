package com.linagora.gatling.imap.scenario

import com.linagora.gatling.imap.PreDef._
import com.linagora.gatling.imap.protocol.{Messages, Recent, StatusItem, StatusItems, UidNext, Unseen}
import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder

import scala.collection.immutable.Seq

object ImapStatusScenario {
  val items: Seq[StatusItem] = Seq(Recent, Messages, Unseen, UidNext)

  def apply(feeder: FeederBuilder): ScenarioBuilder = {
    scenario("Imap")
      .feed(feeder)
      .exec(imap("Connect").connect()).exitHereIfFailed
      .exec(imap("login").login("#{username}", "#{password}").check(ok))
      .exec(imap("status").status("INBOX", StatusItems(items)).check(ok))
  }
}
