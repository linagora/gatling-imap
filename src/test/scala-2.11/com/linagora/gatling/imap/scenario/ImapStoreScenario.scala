package com.linagora.gatling.imap.scenario

import java.util.Calendar

import com.linagora.gatling.imap.PreDef._
import com.linagora.gatling.imap.protocol.command.MessageRange._
import com.linagora.gatling.imap.protocol.command.{MessageRanges, StoreFlags}
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

import scala.collection.immutable.Seq
import scala.concurrent.duration._

class ImapStoreScenario extends Simulation {
  val numOfMailInInbox = 5000
  val percentageOfMailToExpunge = 20.0
  private val appendGracePeriod = 5 milliseconds
  val silent = true

  val userFeeder = csv("users.csv").circular
  val userCount: Int = userFeeder.records.length

  val populateMailbox = exec(imap("append").append("INBOX", Option.empty[Seq[String]], Option.empty[Calendar],
    """From: expeditor@example.com
      |To: recipient@example.com
      |Subject: test subject
      |
      |Test content
      |abcdefghijklmnopqrstuvwxyz
      |0123456789""".stripMargin).check(ok))

  val populateInbox = repeat(numOfMailInInbox)(pause(appendGracePeriod).exec(populateMailbox))

  private val rangeFlagsUpdates = imap("storeAll").store(MessageRanges(From(1L)), StoreFlags.FlagAdd(silent, "all${loopId}")).check(ok)
  private val singleFlagsUpdate = imap("storeOne").store(MessageRanges(One(1L)), StoreFlags.FlagAdd(silent, "one${loopId}")).check(ok)

  val scn = scenario("Imap")
    .feed(userFeeder)
    .pause(1 second)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .exec(imap("select").select("INBOX").check(ok))
    .exec(populateInbox)
    .pause(1 second)
    .repeat(10, "loopId") (exec(rangeFlagsUpdates).pause(1 second).exec(singleFlagsUpdate))

  setUp(scn.inject(atOnceUsers(1))).protocols(imap.host("localhost"))
}
