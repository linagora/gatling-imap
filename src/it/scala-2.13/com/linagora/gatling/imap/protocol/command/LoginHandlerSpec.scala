package com.linagora.gatling.imap.protocol.command

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.linagora.gatling.imap.Fixture.bart
import com.linagora.gatling.imap.protocol.{Command, Response, UserId}
import com.linagora.gatling.imap.{CyrusServer, ImapTestUtils, RunningServer}
import com.sun.mail.imap.protocol.IMAPResponse
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class LoginHandlerSpec extends AnyWordSpec with ImapTestUtils with BeforeAndAfterEach with Matchers {
  val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

  private val server: RunningServer = CyrusServer.start()

  override def beforeEach(): Unit = {
    server.addUser(bart)
  }

  override protected def afterEach(): Unit = {
    system.terminate()
    server.stop()
  }

  implicit lazy val system: ActorSystem = ActorSystem("LoginHandlerSpec")
  "Login handler" should {
    "send the response back when logged in" in {
      val probe = TestProbe()
      val sessionFuture = connect(server.mappedImapPort())
      sessionFuture.onComplete(session => {
        val handler = system.actorOf(LoginHandler.props(session.get))
        probe.send(handler, Command.Login(UserId(1), bart))
      })
      probe.expectMsgPF(1.minute) {
        case Response.LoggedIn(responses) => responses.isOk shouldBe true
      }
    }
  }

  object IMAPResponseMatchers {

    class HasTagMatcher(tag: String) extends Matcher[IMAPResponse] {
      def apply(left: IMAPResponse): MatchResult = {
        val name = left.getTag
        MatchResult(
          name == tag,
          s"""ImapResponse doesn't have tag "$tag"""",
          s"""ImapResponse has tag "$tag""""
        )
      }
    }

    class IsOkMatcher() extends Matcher[IMAPResponse] {
      def apply(left: IMAPResponse): MatchResult = {
        MatchResult(
          left.isOK,
          s"""ImapResponse isn't OK """,
          s"""ImapResponse is OK """
        )
      }
    }

    def isOk = new IsOkMatcher()

    def hasTag(tag: String) = new HasTagMatcher(tag)
  }

}
