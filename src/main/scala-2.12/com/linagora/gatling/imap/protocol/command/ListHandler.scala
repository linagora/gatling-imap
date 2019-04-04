package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.linagora.gatling.imap.protocol._
import io.gatling.core.akka.BaseActor

import scala.util.{Failure, Success, Try}

object ListHandler {
  def props(session: IMAPSession, tag: Tag) = Props(new ListHandler(session, tag))
}

class ListHandler(session: IMAPSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.List(userId, reference, name) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Listed)(logger)
      context.become(waitCallback(sender()))
      Try(session.executeListCommand(tag.string, reference, name, listener)) match {
        case Success(value) =>
        case Failure(e) =>
          logger.error("ERROR when executing LIST COMMAND", e)
          throw e;
      }
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Listed(response) =>
      sender ! msg
      context.stop(self)
  }

}
