package com.linagora.gatling.imap.scenario

import java.util.Calendar

import com.linagora.gatling.imap.PreDef._
import com.typesafe.scalalogging.LazyLogging
import io.gatling.core.Predef._
import io.gatling.core.session._
import io.gatling.core.scenario.Simulation
import io.gatling.core.session.Expression

import scala.collection.immutable.Seq
import scala.concurrent.duration._
import scala.util.Random

class ImapExpungeScenario extends Simulation with LazyLogging {
  val numberOfMailInInbox = Integer.getInteger("numberOfMailInInbox", 1000).intValue()
  val percentageOfMailToExpunge = Integer.getInteger("percentageOfMailToExpunge", 20).toFloat
  val maxDurationInMinutes = Integer.getInteger("maxDuration", 15).toFloat minutes

  logger.trace(s"numberOfMailInInbox $numberOfMailInInbox")
  logger.trace(s"percentageOfMailToExpunge $percentageOfMailToExpunge")

  val userFeeder = csv("data/users.csv").circular
  def getRandomDeleted(): Boolean = Random.nextFloat() < (percentageOfMailToExpunge/ 100.0)

  def flagsWithRandomDeletion: Expression[Session] = (session: Session) => {
    session.set("flags",
      if (getRandomDeleted())
        Some(Seq("\\Flagged", "\\Deleted"))
      else
        Some(Seq("\\Flagged"))
    )
  }

  val populateMailbox = exec(imap("append").append("INBOX", "${flags}", Option.empty[Calendar],
    """From: expeditor@example.com
      |To: recipient@example.com
      |Subject: test subject
      |
      |Test content
      |abcdefghijklmnopqrstuvwxyz
      |0123456789""".stripMargin).check(ok))

  val populateInbox = repeat(numberOfMailInInbox)(exec(flagsWithRandomDeletion).pause(5 millisecond).exec(populateMailbox))

  val scn = scenario("Imap")
    .feed(userFeeder)
    .pause(1 second)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .exec(imap("select").select("INBOX").check(ok))
    .exec(populateInbox)
    .exec(imap("expunge").expunge().check(ok))

  setUp(scn.inject(atOnceUsers(1))).protocols(imap.host("localhost")).maxDuration(maxDurationInMinutes)
}
