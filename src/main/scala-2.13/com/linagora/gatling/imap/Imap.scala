package com.linagora.gatling.imap

import com.sun.mail.imap.protocol.IMAPResponse
import com.yahoo.imapnio.async.client.{ImapAsyncSession, ImapFuture}
import com.yahoo.imapnio.async.request._
import com.yahoo.imapnio.async.response.ImapAsyncResponse

import scala.concurrent.{ExecutionContext, Future}

object Imap {
  def login(login: String, password: String)(implicit session: ImapAsyncSession, executionContext: ExecutionContext): Future[List[IMAPResponse]] =
    executeCommand(session.execute(new LoginCommand(login, password)))

  def rawCommand(command: ImapRequest)(implicit session: ImapAsyncSession, executionContext: ExecutionContext): Future[List[IMAPResponse]] =
    executeCommand(session.execute(command))

  def disconnect()(implicit session: ImapAsyncSession, executionContext: ExecutionContext): Future[Unit] =
    Future.successful(session.close())

  private def executeCommand(command: ImapFuture[ImapAsyncResponse])(implicit executionContext: ExecutionContext) = {
    Future {
      import scala.jdk.CollectionConverters._
      command.get().getResponseLines.asScala.toList
    }
  }
}
