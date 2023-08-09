package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.action.BaseActor
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.CopyMessageCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse

object CopyHandler {
  def props(session: ImapAsyncSession) = Props(new CopyHandler(session))
}

class CopyHandler(session: ImapAsyncSession) extends BaseActor {
  override def receive: Receive = {
    case Command.Copy(userId, range, mailbox) =>
      logger.trace(s"CopyHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitForLoggedIn(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import scala.jdk.CollectionConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.toSeq)
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.CopyResult(responsesList)
      }
      val errorCallback: Consumer[Exception] = e => {
        logger.trace(s"${getClass.getSimpleName} command failed", e)
        logger.error(s"${getClass.getSimpleName} command failed")
        sender ! e
        context.stop(self)
      }

      val future = session.execute(new CopyMessageCommand(range.asImap, mailbox))
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@Response.CopyResult(_) =>
      logger.trace(s"CopyHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }
}

