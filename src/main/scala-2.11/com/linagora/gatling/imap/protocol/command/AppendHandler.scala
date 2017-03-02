package com.linagora.gatling.imap.protocol.command

import java.util

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.linagora.gatling.imap.protocol.{Command, ImapResponses, Response, Tag}
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

object AppendHandler {
  def props(session: IMAPSession, tag: Tag) = Props(new AppendHandler(session, tag))
}

class AppendHandler(session: IMAPSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.Append(userId, mailbox, flags, date, content) =>
      if (! date.isEmpty) throw new NotImplementedError("Date parameter for APPEND is still not implemented")
      val listener = new AppendListener(userId, content)
      val flagsAsString = flags.map(_.mkString("(", " ", ")")).getOrElse("()")
      val length = content.length + content.lines.length - 1
      session.executeAppendCommand(tag.string, mailbox, flagsAsString, length.toString, listener)
      context.become(waitCallback(sender()))
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Appended(response) =>
      sender ! msg
      context.stop(self)
  }


  class AppendListener(userId: String, content: String) extends IMAPCommandListener {

    import collection.JavaConverters._

    override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
      logger.trace(s"Untagged message for $userId : ${response.toString}")
      if (response.isContinuation) {
        content.lines.foreach(session.executeRawTextCommand(_))
      }
    }

    override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
      val response = ImapResponses(responses.asScala.to[Seq])
      logger.trace(s"On response for $userId :\n ${response.mkString("\n")}\n ${sender.path}")
      self ! Response.Appended(response)
    }
  }

}

