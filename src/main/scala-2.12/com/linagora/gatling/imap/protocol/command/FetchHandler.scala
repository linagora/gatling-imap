package com.linagora.gatling.imap.protocol.command

import java.util

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.linagora.gatling.imap.protocol.{Command, ImapResponses, Response, Tag, UserId}
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq





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
      val listener = new FetchListener(userId)

      session.executeTaggedRawTextCommand(tag.string, s"FETCH ${sequence.asString} ${attributes.asString}", listener)
      context.become(waitCallback(sender()))
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Fetched(response) =>
      sender ! msg
      context.stop(self)
  }


  class FetchListener(userId: UserId) extends IMAPCommandListener {

    import collection.JavaConverters._

    override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
      logger.trace(s"Untagged message for $userId : ${response.toString}")
    }

    override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
      val response = ImapResponses(responses.asScala.to[Seq])
      logger.trace(s"On response for $userId :\n ${response.mkString("\n")}\n ${sender.path}")
      self ! Response.Fetched(response)
    }
  }

}

