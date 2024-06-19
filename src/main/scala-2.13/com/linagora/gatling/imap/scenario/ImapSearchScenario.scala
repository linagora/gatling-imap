package com.linagora.gatling.imap.scenario

import java.util.Calendar

import com.linagora.gatling.imap.PreDef._
import com.linagora.gatling.imap.protocol.command.FetchAttributes.AttributeList
import com.linagora.gatling.imap.protocol.command.MessageRange._
import com.linagora.gatling.imap.protocol.command.MessageRanges
import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import javax.mail.Flags
import javax.mail.search.FlagTerm

import scala.collection.immutable.Seq
import scala.concurrent.duration._

object ImapSearchScenario {
  val numberOfMailInInbox = 10
  private val appendGracePeriod = 5 milliseconds

  private val populateMailbox = exec(imap("append").append("INBOX", Option.empty[Seq[String]], Option.empty[Calendar],
    """From: expeditor@example.com
      |To: recipient@example.com
      |Subject: test subject
      |
      |Test content
      |abcdefghijklmnopqrstuvwxyz
      |0123456789""".stripMargin).check(ok))

  private val populateInbox = repeat(numberOfMailInInbox)(pause(appendGracePeriod).exec(populateMailbox))

  def apply(feeder: FeederBuilder): ScenarioBuilder =
    scenario("Imap")
      .feed(feeder)
      .exec(imap("Connect").connect()).exitHereIfFailed
      .exec(imap("login").login("#{username}", "#{password}").check(ok))
      .exec(imap("select").select("INBOX").check(ok))
      .exec(populateInbox)
      .pause(1 second)
      .exec(imap("search").search(MessageRanges(Range(1, numberOfMailInInbox)), new FlagTerm(new Flags(Flags.Flag.SEEN), true)).check(ok))
      .exec(imap("search").uidSearch(MessageRanges(Range(1, numberOfMailInInbox)), new FlagTerm(new Flags(Flags.Flag.SEEN), true)).check(ok))

}
