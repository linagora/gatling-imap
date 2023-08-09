package com.linagora.gatling.imap.protocol.command

import java.util.Properties

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.linagora.gatling.imap.Fixture.bart
import com.linagora.gatling.imap.protocol.{Command, ImapProtocol, ImapResponses, ImapSessions, Response, UserId}
import com.linagora.gatling.imap.{CyrusServer, ImapTestUtils, RunningServer}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._

class ImapSessionsSpec extends AnyWordSpec with Matchers with ImapTestUtils with BeforeAndAfterEach {
  val logger: Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

  private val server: RunningServer = CyrusServer.start()

  override def beforeEach(): Unit = {
    server.addUser(bart)
  }

  override protected def afterEach(): Unit = {
    system.terminate()
    server.stop()
  }

  implicit lazy val system: ActorSystem = ActorSystem("LoginHandlerSpec")
  "the imap sessions actor" should {
    "log a user in" in {
      val config = new Properties()
      val protocol = ImapProtocol("localhost", server.mappedImapPort(), "imap", config)

      val sessions = system.actorOf(ImapSessions.props(protocol))
      val probe = TestProbe()
      val userId = UserId(1)
      probe.send(sessions, Command.Connect(userId))
      probe.expectMsg(10.second, Response.Connected(ImapResponses.empty))
      probe.send(sessions, Command.Login(userId, bart))
      probe.expectMsgPF(10.second) {
        case Response.LoggedIn(responses: ImapResponses) => responses.isOk shouldBe true
      }
    }
  }

}
