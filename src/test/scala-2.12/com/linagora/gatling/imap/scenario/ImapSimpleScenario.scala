package com.linagora.gatling.imap.scenario

import java.util.Calendar

import com.linagora.gatling.imap.PreDef._
import com.linagora.gatling.imap.protocol.command.FetchAttributes.AttributeList
import com.linagora.gatling.imap.protocol.command.MessageRange._
import com.linagora.gatling.imap.protocol.command.{MessageRanges, Silent, StoreFlags}
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

import scala.collection.immutable.Seq
import scala.concurrent.duration._

class ImapSimpleScenario extends Simulation {
  val UserCount: Int = 3
  val feeder = (0 until UserCount).map(i => Map("username" -> s"user$i", "password" -> "password")).toArray

  val receiveEmail = exec(imap("append").append("INBOX", Some(Seq("\\Flagged")), Option.empty[Calendar],
    """From: expeditor@example.com
      |To: recipient@example.com
      |Subject: test subject
      |
      |Test content
      |abcdefghijklmnopqrstuvwxyz
      |0123456789""".stripMargin).check(ok))

  val scn = scenario("Imap").feed(feeder)
    .pause(1 second)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .during(1 minute) {
        exec(imap("list").list("", "*").check(ok, hasFolder("INBOX")))
        .pause(200 milli)
        .exec(imap("select").select("INBOX").check(ok))
        .pause(200 milli)
        .exec(receiveEmail)
        .pause(200 milli)
        .exec(imap("fetch").fetch(MessageRanges(Last()), AttributeList("BODY[HEADER]", "UID", "BODY[TEXT]")).check(ok))
        .pause(200 milli)
        .exec(imap("store").store(MessageRanges(Last()), StoreFlags.FlagAdd(Silent.Disable(), "\\Deleted")).check(ok))
        .pause(200 milli)
        .exec(imap("expunge").expunge().check(ok))
        .pause(200 milli)
        .exec(imap("fetch").fetch(MessageRanges(Last()), AttributeList("BODY[HEADER]", "UID", "BODY[TEXT]")).check(no))
    }

  setUp(scn.inject(nothingFor(1 seconds), rampUsers(UserCount) during (10 seconds))).protocols(imap.host("localhost"))
}
