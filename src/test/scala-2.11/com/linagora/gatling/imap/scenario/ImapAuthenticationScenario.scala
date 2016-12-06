package com.linagora.gatling.imap.scenario

import java.util.Calendar

import com.linagora.gatling.imap.PreDef._
import com.linagora.gatling.imap.protocol.Uid
import com.linagora.gatling.imap.protocol.command.FetchAttributes.AttributeList
import com.linagora.gatling.imap.protocol.command.FetchRange.{From, One, Range, To}
import io.gatling.app.Gatling
import io.gatling.core.Predef._
import io.gatling.core.config.GatlingPropertiesBuilder
import io.gatling.core.scenario.Simulation

import scala.collection.immutable.Seq
import scala.concurrent.duration._

class ImapAuthenticationScenario extends Simulation {
  val feeder = Array(Map("username" -> "user1", "password" -> "password")).circular
  val UserCount: Int = 10

  val scn = scenario("ImapAuthentication").feed(feeder)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .exec(imap("list").list("", "*").check(ok, hasFolder("INBOX")))
    .exec(imap("select").select("INBOX").check(ok, hasRecent(0)))
    .exec(imap("append").append("INBOX", Some(Seq("\\Flagged")), Option.empty[Calendar],
      """From: expeditor@example.com
        |To: recipient@example.com
        |Subject: test subject
        |
        |Test content""".stripMargin).check(ok))
    .exec(imap("fetch").fetch(Seq(One(1), One(2), Range(3,5), From(3), One(8), To(1)), AttributeList("BODY", "UID")).check(ok, hasUid(Uid(1)), contains("TEXT")))

  setUp(scn.inject(constantUsersPerSec(UserCount).during(2.seconds))).protocols(imap.host("localhost"))
}

object Engine extends App {
  // This sets the class for the simulation we want to run.
  val simClass = classOf[ImapAuthenticationScenario].getName

  val props = new GatlingPropertiesBuilder
  props.sourcesDirectory("./src/main/scala")
  props.binariesDirectory("./target/scala-2.11/classes")
  props.simulationClass(simClass)

  Gatling.fromMap(props.build)

}
