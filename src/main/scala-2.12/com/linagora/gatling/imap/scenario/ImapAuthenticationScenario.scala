package com.linagora.gatling.imap.scenario

import com.linagora.gatling.imap.PreDef.{contains, hasFolder, hasRecent, hasUid, imap, ok}
import com.linagora.gatling.imap.protocol.Uid
import com.linagora.gatling.imap.protocol.command.FetchAttributes.AttributeList
import com.linagora.gatling.imap.protocol.command.MessageRange.{From, One, Range, To}
import com.linagora.gatling.imap.protocol.command.MessageRanges
import io.gatling.core.Predef.{scenario, _}
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder

import scala.collection.immutable.Seq

object ImapAuthenticationScenario {

  def apply(feeder: FeederBuilder): ScenarioBuilder = scenario("ImapAuthentication")
    .feed(feeder)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .exec(imap("list").list("", "*").check(ok, hasFolder("INBOX")))
    .exec(imap("select").select("INBOX").check(ok, hasRecent(0)))
    .exec(imap("append").append(mailbox = "INBOX", flags = Some(Seq("\\Flagged")), date = None,
      content =
        """From: expeditor@example.com
        |To: recipient@example.com
        |Subject: test subject
        |
        |Test content""".stripMargin).check(ok))
    .exec(imap("fetch").fetch(MessageRanges(One(1), One(2), Range(3,5), From(3), One(8), To(1)), AttributeList("BODY", "UID")).check(ok, hasUid(Uid(1)), contains("TEXT")))
    .exec(imap("Disconnect").disconnect())

}
