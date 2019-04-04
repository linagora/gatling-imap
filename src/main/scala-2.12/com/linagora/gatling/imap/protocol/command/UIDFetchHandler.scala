package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.linagora.gatling.imap.protocol._
import io.gatling.core.akka.BaseActor

object UIDFetchHandler {
  def props(session: IMAPSession, tag: Tag) = Props(new UIDFetchHandler(session, tag))
}

class UIDFetchHandler(session: IMAPSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.UIDFetch(userId, sequence, attributes) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Fetched)(logger)
      context.become(waitCallback(sender()))
      session.executeTaggedRawTextCommand(tag.string, s"UID FETCH ${sequence.asString} ${attributes.asString}", listener)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Fetched(response) =>
      sender ! msg
      context.stop(self)
  }

}
