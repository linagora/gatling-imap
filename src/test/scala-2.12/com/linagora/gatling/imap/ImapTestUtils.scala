package com.linagora.gatling.imap

import java.net.URI
import java.util
import java.util.Properties

import com.lafaspot.imapnio.client.{IMAPClient, IMAPSession}
import com.lafaspot.imapnio.listener.IMAPConnectionListener
import com.lafaspot.logfast.logging
import com.lafaspot.logfast.logging.LogManager
import com.lafaspot.logfast.logging.internal.LogPage
import com.sun.mail.imap.protocol.IMAPResponse
import org.slf4j.Logger

trait ImapTestUtils {

  def logger: Logger

  val threadNumber = 4
  val config = new Properties()
  val imapClient = new IMAPClient(threadNumber)

  def withConnectedSession(port: Int)(f: IMAPSession => Unit) = {
    val uri = new URI(s"imap://localhost:$port")
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
    val session = imapClient.createSession(uri, config, connectionListener, new LogManager(logging.Logger.Level.DEBUG, LogPage.DEFAULT_SIZE))
    logger.trace(s"connection to $uri")
    session.connect()
  }

}
