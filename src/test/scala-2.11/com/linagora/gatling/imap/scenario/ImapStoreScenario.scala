package com.linagora.gatling.imap.scenario

import java.util.Calendar

import com.linagora.gatling.imap.PreDef._
import com.linagora.gatling.imap.protocol.command.MessageRange._
import com.linagora.gatling.imap.protocol.command.{MessageRanges, Silent, StoreFlags}
import io.gatling.core.Predef._

import scala.collection.immutable.Seq
import scala.concurrent.duration._

object ImapStoreScenario {
  val numberOfMailInInbox = 5000
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

  val rangeFlagsUpdates = imap("storeAll").store(MessageRanges(From(1L)), StoreFlags.FlagAdd(Silent.Enable(), "all${loopId}")).check(ok)
  val singleFlagsUpdate = imap("storeOne").store(MessageRanges(One(1L)), StoreFlags.FlagAdd(Silent.Enable(), "one${loopId}")).check(ok)

  val storeScenario = exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .exec(imap("select").select("INBOX").check(ok))
    .exec(populateInbox)
    .pause(1 second)
    .repeat(10, "loopId") (exec(rangeFlagsUpdates).pause(1 second).exec(singleFlagsUpdate))
}
