package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.channel.IMAPChannelFuture
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPChannelFutureListener
import com.linagora.gatling.imap.protocol._
import io.gatling.core.akka.BaseActor

import scala.util.{Failure, Success, Try}

object LoginHandler {
  def props(session: IMAPSession, tag: Tag) = Props(new LoginHandler(session, tag))
}

class LoginHandler(session: IMAPSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.Login(userId, user, password) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.LoggedIn)(logger)
      logger.trace(s"LoginHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitForLoggedIn(sender()))
      Try(session.executeLoginCommand(tag.string, user, password, listener)) match {
        case Success(futureResult) =>
          futureResult.addListener(new IMAPChannelFutureListener {
            override def operationComplete(future: IMAPChannelFuture): Unit = {
              logger.debug(s"LoginHandler command completed, success : ${future.isSuccess}")
              if (!future.isSuccess) {
                logger.error("LoginHandler command failed", future.cause())
              }
            }
          })
        case Failure(e) =>
          logger.error("ERROR when executing LOGIN COMMAND", e)
          throw e
      }
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@Response.LoggedIn(response) =>
      logger.trace(s"LoginHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }

}

