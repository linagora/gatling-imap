package com.linagora.gatling.imap

import java.net.URL

import com.linagora.gatling.imap.protocol.{Domain, User}
import org.slf4j.{Logger, LoggerFactory}
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.MountableFile

import scala.concurrent.Await
import scala.concurrent.duration._

object JamesServer extends Server {

  private val WAIT_TIMEOUT = 30 seconds
  private val jmapPort = 80
  private val imapPort = 143
  private val smtpPort = 587
  private val webadminPort = 8000
  private val logger: Logger = LoggerFactory.getLogger(JamesServer.getClass)

  class RunningJamesServer(container: GenericContainer[_]) extends RunningServer with ImapTestUtils {
    protected val logger: Logger = JamesServer.logger
    lazy val mappedJmapPort: Integer = container.getMappedPort(jmapPort)
    lazy val mappedWebadminPort: Integer = container.getMappedPort(webadminPort)
    lazy val mappedSmtpPort: Integer = container.getMappedPort(smtpPort)
    lazy val mappedImapPort: Integer = container.getMappedPort(imapPort)
    lazy val mappedWebadmin = new JamesWebAdministration(new URL(s"http://localhost:$mappedWebadminPort"))

    def addUser(user: User): Unit = Await.result(mappedWebadmin.addUser(user), WAIT_TIMEOUT)

    def addDomain(domain: Domain): Unit = {
      Await.result(mappedWebadmin.addDomain(domain), WAIT_TIMEOUT)
    }

    def stop(): Unit = container.stop()

  }

  def start(): RunningServer = {
    val james = new GenericContainer("linagora/tmail-backend:memory-0.4.3")
    james.withCopyFileToContainer(MountableFile.forClasspathResource("james-conf/jwt_privatekey"), "/root/conf/")
    james.withCopyFileToContainer(MountableFile.forClasspathResource("james-conf/jwt_publickey"), "/root/conf/")
    james.withCopyFileToContainer(MountableFile.forClasspathResource("james-conf/webadmin.properties"), "/root/conf/")
    james.withCopyFileToContainer(MountableFile.forClasspathResource("james-conf/keystore"), "/root/conf/")
    james.withCopyFileToContainer(MountableFile.forClasspathResource("james-conf/imapserver.xml"), "/root/conf/")
    james.withCopyFileToContainer(MountableFile.forClasspathResource("james-conf/smtpserver.xml"), "/root/conf/")
    james.addExposedPorts(jmapPort, imapPort, smtpPort, webadminPort)
    james.start()
    new RunningJamesServer(james)
  }

}
