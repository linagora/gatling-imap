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

class ImapFullScenario extends Simulation {
  val feeder = csv("users.csv").circular
  val userCount: Int = feeder.records.length

  val receiveEmail = exec(imap("append").append("INBOX", Some(Seq("\\Flagged")), Option.empty[Calendar],
                  """From: expeditor@example.com
                    |To: recipient@example.com
                    |Subject: test subject
                    |
                    |Test content
                    |abcdefghijklmnopqrstuvwxyz
                    |0123456789""".stripMargin).check(ok))

  val readLastEmail = exec(imap("list").list("", "*").check(ok, hasFolder("INBOX")))
                      .exec(imap("select").select("INBOX").check(ok))
                      .exec(imap("fetch").fetch(MessageRanges(Last()), AttributeList("BODY[HEADER]", "UID", "BODY[TEXT]")).check(ok))

  val heavyUser = repeat(3)(receiveEmail)
                  .repeat(2)(readLastEmail)
  val lightUser = receiveEmail
                  .exec(readLastEmail)

  val scn = scenario("Imap").feed(feeder)
    .pause(1 second)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .exec {
      randomSwitch(
        75.0 -> forever() {
                  pace(5 minutes, 15 minutes)
                  .exec(lightUser)
                },

        25.0 -> forever() {
                  pace(30 seconds, 90 seconds)
                  .exec(heavyUser)
                }
      )

    }

  val userCountWith50PercentsOfThemAlsoReadingTheirEmailOnAnotherDevice = (userCount * 1.5).toInt

  setUp(scn.
      inject(
          nothingFor(1 seconds),
          rampUsers(userCountWith50PercentsOfThemAlsoReadingTheirEmailOnAnotherDevice) over(10 minutes)))
    .protocols(imap.host("localhost"))
    .maxDuration(3 hours)
}
