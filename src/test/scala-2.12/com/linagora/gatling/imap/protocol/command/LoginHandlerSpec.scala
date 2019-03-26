package com.linagora.gatling.imap.protocol.command

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.linagora.gatling.imap.protocol.{Command, Response, Tag}
import com.linagora.gatling.imap.{CyrusServer, Imap, ImapTestUtils}
import com.sun.mail.imap.protocol.IMAPResponse
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.slf4j
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}


class LoginHandlerSpec extends WordSpec with ImapTestUtils with BeforeAndAfterEach with Matchers {
  val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

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
  "Login handler" should {
    "send the response back when logged in" in {
      val tag = Tag.initial
      val probe = TestProbe()
      withConnectedSession (cyrusServer.mappedImapPort) { session =>
        val handler = system.actorOf(LoginHandler.props(session, tag))
        probe.send(handler, Command.Login("userId1", "user1", "password"))
      }
      probe.expectMsgPF(1.minute) {
        case Response.LoggedIn(responses) => responses.isOk shouldBe true
      }
    }
  }

  object IMAPResponseMatchers {

    class HasTagMatcher(tag: String) extends Matcher[IMAPResponse] {
      def apply(left: IMAPResponse) = {
        val name = left.getTag
        MatchResult(
          name == tag,
          s"""ImapResponse doesn't have tag "$tag"""",
          s"""ImapResponse has tag "$tag""""
        )
      }
    }

    class IsOkMatcher() extends Matcher[IMAPResponse] {
      def apply(left: IMAPResponse) = {
        MatchResult(
          left.isOK,
          s"""ImapResponse isn't OK """,
          s"""ImapResponse is OK """
        )
      }
    }

    def isOk() = new IsOkMatcher()

    def hasTag(tag: String) = new HasTagMatcher(tag)
  }

}
