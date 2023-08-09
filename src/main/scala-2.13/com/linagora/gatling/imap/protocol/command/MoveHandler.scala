package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.action.BaseActor
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.MoveMessageCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse

object MoveHandler {
  def props(session: ImapAsyncSession) = Props(new MoveHandler(session))
}

class MoveHandler(session: ImapAsyncSession) extends BaseActor {
  override def receive: Receive = {
    case Command.Move(userId, range, mailbox) =>
      logger.trace(s"MoveHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitForLoggedIn(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import scala.jdk.CollectionConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.toSeq)
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.MoveResult(responsesList)
      }
      val errorCallback: Consumer[Exception] = e => {
        logger.trace(s"${getClass.getSimpleName} command failed", e)
        logger.error(s"${getClass.getSimpleName} command failed")
        sender ! e
        context.stop(self)
      }

      val future = session.execute(new MoveMessageCommand(range.asImap, mailbox))
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@Response.MoveResult(_) =>
      logger.trace(s"MoveHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }
}

