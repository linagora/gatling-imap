package com.linagora.gatling.imap.scenario

import java.util.Calendar

import com.linagora.gatling.imap.PreDef._
import com.linagora.gatling.imap.protocol.command.FetchAttributes.AttributeList
import com.linagora.gatling.imap.protocol.command.MessageRange._
import com.linagora.gatling.imap.protocol.command.MessageRanges
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

import scala.collection.immutable.Seq
import scala.concurrent.duration._

class ImapUIDFetchScenario extends Simulation {
  val feeder = csv("users.csv").circular
  val numberOfMailInInbox = 1000
  private val appendGracePeriod = 5 milliseconds

  val populateMailbox = exec(imap("append").append("INBOX", Option.empty[Seq[String]], Option.empty[Calendar],
    """From: expeditor@example.com
      |To: recipient@example.com
      |Subject: test subject
      |
      |Test content
      |abcdefghijklmnopqrstuvwxyz
      |0123456789""".stripMargin).check(ok))

  val populateInbox = repeat(numberOfMailInInbox)(pause(appendGracePeriod).exec(populateMailbox))

  val uidFetch = exec(imap("uidFetch").uidFetch(MessageRanges(Range(1, numberOfMailInInbox)), AttributeList("UID")).check(ok))
  
  val scn = scenario("Imap").feed(feeder)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .exec(imap("select").select("INBOX").check(ok))
    .exec(populateInbox)
    .pause(1 second)
    .exec(repeat(100)(uidFetch))

  setUp(scn.inject(atOnceUsers(1))).protocols(imap.host("localhost"))
}
