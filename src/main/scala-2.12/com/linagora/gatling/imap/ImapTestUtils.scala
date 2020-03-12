package com.linagora.gatling.imap

import java.net.URI
import java.util.Properties

import org.slf4j.Logger

import com.yahoo.imapnio.async.client.ImapAsyncSession.DebugMode
import com.yahoo.imapnio.async.client.{ImapAsyncClient, ImapAsyncSession, ImapAsyncSessionConfig}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait ImapTestUtils {

  protected def logger: Logger

  val threadNumber = 4
  val config = new Properties()
  val imapClient = new ImapAsyncClient(threadNumber)

  def connect(port: Int): Future[ImapAsyncSession] = {
    val serverUri = new URI(s"imap://localhost:$port")
    val config = new ImapAsyncSessionConfig
    config.setConnectionTimeoutMillis(5000)
    config.setReadTimeoutMillis(6000)
    val sniNames = null

    val localAddress = null
    Future {
      imapClient
        .createSession(serverUri, config, localAddress, sniNames, DebugMode.DEBUG_ON, "ImapTestUtilsCreated")
        .get()
        .getSession
    }
  }

}
