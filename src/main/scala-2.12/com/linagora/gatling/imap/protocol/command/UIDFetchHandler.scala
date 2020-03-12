package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.UidFetchCommand
import io.gatling.core.akka.BaseActor

object UIDFetchHandler {
  def props(session: ImapAsyncSession) = Props(new UIDFetchHandler(session))
}

class UIDFetchHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.UIDFetch(userId, sequence, attributes) =>
      context.become(waitCallback(sender()))
      ImapSessionExecutor.listen(self, userId, Response.Fetched)(logger)(session.execute(new UidFetchCommand(sequence.asImap, attributes.asString)))

  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Fetched(_) =>
      sender ! msg
      context.stop(self)
  }

}
