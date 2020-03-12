package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.SelectFolderCommand
import io.gatling.core.akka.BaseActor

object SelectHandler {
  def props(session: ImapAsyncSession) = Props(new SelectHandler(session))
}

class SelectHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.Select(userId, mailbox) =>
      context.become(waitCallback(sender()))
      ImapSessionExecutor.listen(self, userId, Response.Selected)(logger)(session.execute(new SelectFolderCommand(mailbox)))
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Selected(_) =>
      sender ! msg
      context.stop(self)
  }

}
