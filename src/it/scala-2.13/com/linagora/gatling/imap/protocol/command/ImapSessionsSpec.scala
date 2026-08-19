package com.linagora.gatling.imap.protocol.command

import java.util.Properties

import com.linagora.gatling.imap.Fixture.bart
import com.linagora.gatling.imap.protocol.{ImapComponents, ImapProtocol, ImapResponses, UserId}
import com.linagora.gatling.imap.{Fixture, ImapTestUtils, JamesServer, RunningServer}
import com.yahoo.imapnio.async.request.LoginCommand
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.jdk.CollectionConverters._

class ImapSessionsSpec extends AnyWordSpec with Matchers with ImapTestUtils with BeforeAndAfterEach {
  val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

  private val server: RunningServer = JamesServer.start()

  override def beforeEach(): Unit = {
    server.addDomain(Fixture.simpson)
    server.addUser(bart)
  }

  override protected def afterEach(): Unit = {
    server.stop()
  }

  "the imap sessions" should {
    "log a user in" in {
      implicit val executionContext: ExecutionContext = ExecutionContext.global
      val config = new Properties()
      val protocol = ImapProtocol("localhost", server.mappedImapPort(), "imap", config)

      val components = ImapComponents(protocol)
      val connected = Promise[com.yahoo.imapnio.async.client.ImapAsyncSession]()
      components.connect(UserId(1))(s => connected.success(s), e => connected.failure(e))
      val session = Await.result(connected.future, 10.seconds)
      val future = session.execute(new LoginCommand(bart.login, bart.password))
      val responses = Await.result(
        Future { ImapResponses(future.get().getResponseLines.asScala.toSeq) },
        10.seconds)
      responses.isOk shouldBe true
      components.disconnect(UserId(1))
    }
  }
}