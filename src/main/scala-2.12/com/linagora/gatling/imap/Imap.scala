package com.linagora.gatling.imap

import java.util

import com.lafaspot.imapnio.channel.IMAPChannelFuture
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.sun.mail.imap.protocol.IMAPResponse

import scala.concurrent.{ExecutionContext, Future, Promise}

object Imap {
  case class TaggedResponse(tag: String, responses: List[IMAPResponse])

  def login(login: String, password: String)(implicit session: IMAPSession, executionContext: ExecutionContext): Future[TaggedResponse] =
    executeCommand(session.executeLoginCommand("A1", login, password, _))

  def rawCommand(str: String)(implicit session: IMAPSession, executionContext: ExecutionContext): Future[TaggedResponse] =
    executeCommand(session.executeTaggedRawTextCommand("A1", str, _))

  def disconnect()(implicit session: IMAPSession, executionContext: ExecutionContext): Future[Unit] =
    Future.successful(session.disconnect())

  private def executeCommand(command: IMAPCommandListener => IMAPChannelFuture)(implicit executionContext: ExecutionContext) = {
    val taggedResponse = Promise[TaggedResponse]()
    command(new IMAPCommandListener {
      override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
        import collection.JavaConverters._
        taggedResponse.success(TaggedResponse(tag, responses.asScala.toList))
      }

      override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {}
    })
    taggedResponse.future
  }
}
