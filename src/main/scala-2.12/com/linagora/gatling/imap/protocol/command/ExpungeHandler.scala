package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.linagora.gatling.imap.protocol.{Response, _}
import io.gatling.core.akka.BaseActor

object ExpungeHandler {
  def props(session: IMAPSession, tag: Tag) = Props(new ExpungeHandler(session, tag))
}

class ExpungeHandler(session: IMAPSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.Expunge(userId) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Expunged)(logger)
      context.become(waitCallback(sender()))
      session.executeTaggedRawTextCommand(tag.string, "EXPUNGE", listener)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Expunged(_) =>
      sender ! msg
      context.stop(self)
  }

}
