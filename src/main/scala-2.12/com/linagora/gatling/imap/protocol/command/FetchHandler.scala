package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.linagora.gatling.imap.protocol._
import io.gatling.core.akka.BaseActor


abstract class FetchAttributes {
  def asString: String
}

object FetchAttributes {

  case class ALL() extends FetchAttributes {
    override def asString = "ALL"
  }

  case class FULL() extends FetchAttributes {
    override def asString = "FULL"
  }

  case class FAST() extends FetchAttributes {
    override def asString = "FAST"
  }

  case class AttributeList(fetchAttributes: String*) extends FetchAttributes {
    override def asString = fetchAttributes.mkString("(", " ", ")")
  }

}

object FetchHandler {
  def props(session: IMAPSession, tag: Tag) = Props(new FetchHandler(session, tag))
}

class FetchHandler(session: IMAPSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.Fetch(userId, sequence, attributes) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Fetched)(logger)
      context.become(waitCallback(sender()))
      session.executeTaggedRawTextCommand(tag.string, s"FETCH ${sequence.asString} ${attributes.asString}", listener)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Fetched(response) =>
      sender ! msg
      context.stop(self)
  }

}
