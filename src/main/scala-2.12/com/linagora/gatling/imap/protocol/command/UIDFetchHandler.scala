package com.linagora.gatling.imap.protocol.command

import java.util

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.linagora.gatling.imap.protocol.{Command, ImapResponses, Response, UserId, Tag}
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

object UIDFetchHandler {
  def props(session: IMAPSession, tag: Tag) = Props(new UIDFetchHandler(session, tag))
}

class UIDFetchHandler(session: IMAPSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.UIDFetch(userId, sequence, attributes) =>
      val listener = new UIDFetchListener(userId)

      session.executeTaggedRawTextCommand(tag.string, s"UID FETCH ${sequence.asString} ${attributes.asString}", listener)
      context.become(waitCallback(sender()))
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Fetched(response) =>
      sender ! msg
      context.stop(self)
  }


  class UIDFetchListener(userId: UserId) extends IMAPCommandListener {

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

