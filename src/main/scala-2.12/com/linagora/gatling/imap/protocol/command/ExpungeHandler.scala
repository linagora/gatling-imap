package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol.{Response, _}
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.ExpungeCommand
import io.gatling.core.akka.BaseActor

object ExpungeHandler {
  def props(session: ImapAsyncSession) = Props(new ExpungeHandler(session))
}

class ExpungeHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.Expunge(userId) =>
      context.become(waitCallback(sender()))
      ImapSessionExecutor.listen(self, userId, Response.Expunged)(logger)(session.execute(new ExpungeCommand()))
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Expunged(_) =>
      sender ! msg
      context.stop(self)
  }

}
