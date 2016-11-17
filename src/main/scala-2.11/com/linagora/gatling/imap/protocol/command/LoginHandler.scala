package com.linagora.gatling.imap.protocol.command

import java.util

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.linagora.gatling.imap.protocol.{Command, ImapResponses, Response, Tag}
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

object LoginHandler {
  def props(session: IMAPSession, tag: Tag) = Props(new LoginHandler(session, tag))
}

class LoginHandler(session: IMAPSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.Login(userId, user, password) =>
      val listener = new LoginListener(userId)
      session.executeLoginCommand(tag.string, user, password, listener)
      context.become(waitForLoggedIn(sender()))
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@Response.LoggedIn(response) =>
      sender ! msg
      context.stop(self)
  }

  class LoginListener(userId: String) extends IMAPCommandListener {

    import collection.JavaConverters._

    override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
      logger.trace(s"Untagged message for $userId : ${response.toString}")
    }

    override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
      val response = ImapResponses(responses.asScala.to[Seq])
      logger.trace(s"On response for $userId :\n ${response.mkString("\n")}\n ${sender.path}")
      self ! Response.LoggedIn(response)
    }
  }

}

