package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.LoginCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import io.gatling.core.akka.BaseActor

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object LoginHandler {
  def props(session: ImapAsyncSession) = Props(new LoginHandler(session))
}

class LoginHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.Login(userId, user, password) =>
      logger.trace(s"LoginHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitForLoggedIn(sender()))
      ImapSessionExecutor.listenWithHandler(self, userId, Response.LoggedIn, callback)(logger)(session.execute(new LoginCommand(user, password)))
  }

  private def callback(response: Future[ImapAsyncResponse]) = {
    Try(response) match {
      case Success(futureResult) =>
        futureResult.onComplete(future => {
            logger.debug(s"LoginHandler command completed, success : ${future.isSuccess}")
            if (!future.isSuccess) {
              logger.error("LoginHandler command failed", future.toEither.left)
            }
          })
      case Failure(e) =>
        logger.error("ERROR when executing LOGIN COMMAND", e)
        throw e
    }
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@Response.LoggedIn(_) =>
      logger.trace(s"LoginHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }

}

