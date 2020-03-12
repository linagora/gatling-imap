package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.FetchCommand
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
    override def asString = fetchAttributes.mkString(" ")
  }

}

object FetchHandler {
  def props(session: ImapAsyncSession) = Props(new FetchHandler(session))
}

class FetchHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.Fetch(userId, sequence, attributes) =>
      context.become(waitCallback(sender()))
      ImapSessionExecutor.listen(self, userId, Response.Fetched)(logger)(session.execute(new FetchCommand(sequence.asImap, attributes.asString)))
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Fetched(_) =>
      sender ! msg
      context.stop(self)
  }

}
