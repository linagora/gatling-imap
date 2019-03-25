package com.linagora.gatling.imap.protocol

import java.util.Properties

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.linagora.gatling.imap.{CyrusServer, Imap, ImapTestUtils}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}

class ImapSessionsSpec extends WordSpec with Matchers with ImapTestUtils with BeforeAndAfterEach {
  val logger: Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

  private val cyrusServer: CyrusServer.RunningCyrusServer = CyrusServer.start()
    .createUser("user1", "password")

  override def beforeEach(): Unit = {
    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    withConnectedSession (cyrusServer.mappedImapPort) { implicit session =>
      Await.result(for {
        _ <- Imap.login("cyrus", "cyrus")
        _ <- Imap.rawCommand("CREATE user.user1")
        _ <- Imap.disconnect()
      } yield (), 1.minute)
    }
  }

  override protected def afterEach(): Unit = {
    system.terminate()
    cyrusServer.stop()
  }

  implicit lazy val system = ActorSystem("LoginHandlerSpec")
  "the imap sessions actor" should {
    "log a user in" in {
      val config = new Properties()
      val protocol = ImapProtocol("localhost", port = cyrusServer.mappedImapPort, config = config)

      val sessions = system.actorOf(ImapSessions.props(protocol))
      val probe = TestProbe()
      probe.send(sessions, Command.Connect("1"))
      probe.expectMsg(10.second, Response.Connected(ImapResponses.empty))
      probe.send(sessions, Command.Login("1", "user1", "password"))
      probe.expectMsgPF(10.second) {
        case Response.LoggedIn(responses: ImapResponses) => responses.isOk shouldBe true
      }
    }
  }

}
