package com.linagora.gatling.imap.scenario

import java.util.Calendar

import com.linagora.gatling.imap.PreDef._
import com.typesafe.scalalogging.LazyLogging
import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.scenario.Simulation
import io.gatling.core.session.{Expression, _}
import io.gatling.core.structure.ScenarioBuilder

import scala.collection.immutable.Seq
import scala.concurrent.duration._
import scala.util.Random

object ImapExpungeScenario extends Simulation with LazyLogging {
  private val numberOfMailInInbox = Integer.getInteger("numberOfMailInInbox", 1000).intValue()
  private val percentageOfMailToExpunge = Integer.getInteger("percentageOfMailToExpunge", 20).toFloat
  private val maxDurationInMinutes = Integer.getInteger("maxDuration", 15).toFloat minutes

  logger.trace(s"numberOfMailInInbox $numberOfMailInInbox")
  logger.trace(s"percentageOfMailToExpunge $percentageOfMailToExpunge")

  private def getRandomDeleted(): Boolean = Random.nextFloat() < (percentageOfMailToExpunge/ 100.0)

  def flagsWithRandomDeletion: Expression[Session] = (session: Session) => {
    session.set("flags",
      if (getRandomDeleted())
        Some(Seq("\\Flagged", "\\Deleted"))
      else
        Some(Seq("\\Flagged"))
    )
  }

  private val populateMailbox = exec(imap("append").append("INBOX", "${flags}", Option.empty[Calendar],
    """From: expeditor@example.com
      |To: recipient@example.com
      |Subject: test subject
      |
      |Test content
      |abcdefghijklmnopqrstuvwxyz
      |0123456789""".stripMargin).check(ok))

  private val populateInbox = repeat(numberOfMailInInbox)(exec(flagsWithRandomDeletion).pause(5 millisecond).exec(populateMailbox))

  def apply(feeder: FeederBuilder): ScenarioBuilder = scenario("Imap")
    .feed(feeder)
    .pause(1 second)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .exec(imap("select").select("INBOX").check(ok))
    .exec(populateInbox)
    .exec(imap("expunge").expunge().check(ok))

}
