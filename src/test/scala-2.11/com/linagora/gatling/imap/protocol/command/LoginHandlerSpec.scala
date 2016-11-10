package com.linagora.gatling.imap.protocol.command

import java.net.URI
import java.util
import java.util.Properties

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.lafaspot.imapnio.client.{IMAPClient, IMAPSession}
import com.lafaspot.imapnio.listener.IMAPConnectionListener
import com.lafaspot.logfast.logging.internal.LogPage
import com.lafaspot.logfast.logging.{LogManager, Logger}
import com.linagora.gatling.imap.protocol.{Command, Response, Tag}
import com.sun.mail.imap.protocol.IMAPResponse
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._


class LoginHandlerSpec extends WordSpec with BeforeAndAfterAll with Matchers {
  private val logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)
  implicit lazy val system = ActorSystem("LoginHandlerSpec")
  "Login handler" should {
    "send the response back when logged in" in {
      val tag = Tag.initial
      val probe = TestProbe()
      withConnectedSession { session =>
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


  val config = new Properties()
  val uri = new URI("imap://localhost:143")
  val imapClient = new IMAPClient(4)

  def withConnectedSession(f: IMAPSession => Unit) = {
    val connectionListener = new IMAPConnectionListener {
      override def onConnect(session: IMAPSession): Unit = {
        logger.trace("onConnect " + session)

        f(session)
      }

      override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
        logger.trace("onMessage " + response)
      }

      override def onDisconnect(session: IMAPSession, cause: Throwable): Unit =
        logger.trace("disconnected", cause)

      override def onInactivityTimeout(session: IMAPSession): Unit = {
        logger.trace("onInactivityTimeout " + session)
      }

      override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
        logger.trace("onResponse " + responses)
      }
    }
    val session = imapClient.createSession(uri, config, connectionListener, new LogManager(Logger.Level.DEBUG, LogPage.DEFAULT_SIZE))
    println(uri)
    session.connect()
  }

  override protected def afterAll(): Unit = {
    system.terminate()
  }
}
