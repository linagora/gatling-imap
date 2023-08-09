package com.linagora.gatling.imap

import com.linagora.gatling.imap.protocol.{Domain, User}
import org.slf4j.{Logger, LoggerFactory}
import org.testcontainers.containers.GenericContainer

import com.yahoo.imapnio.async.request.CreateFolderCommand

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}

object CyrusServer extends Server {

  private val imapPort = 143
  private val logger: Logger = LoggerFactory.getLogger(CyrusServer.getClass)

  class RunningCyrusServer(val container: GenericContainer[_]) extends RunningServer with ImapTestUtils {
    protected val logger: Logger = CyrusServer.logger
    lazy val mappedImapPort: Integer = container.getMappedPort(imapPort)

    def addUser(user: User): Unit = {
      container.execInContainer("bash", "-c", s"echo ${user.password} | saslpasswd2 -u test -c ${user.login} -p")
      implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
      Await.result(
        connect(mappedImapPort)
          .flatMap(implicit session =>
            for {
              _ <- Imap.login("cyrus", "cyrus")
              _ <- Imap.rawCommand(new CreateFolderCommand(s"user.${user.login}"))
              _ <- Imap.disconnect()
            } yield ()), 1.minute)

    }

    def stop(): Unit = container.stop()

    override def addDomain(domain: Domain): Unit = {
      //do nothing
    }
  }

  def start(): RunningServer = {
    val cyrus = new GenericContainer("linagora/cyrus-imap")
    cyrus.addExposedPort(imapPort)
    cyrus.start()
    new RunningCyrusServer(cyrus)
  }
}
