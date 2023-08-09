package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.action.BaseActor
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.RenameFolderCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse

object RenameFolderHandler {
  def props(session: ImapAsyncSession) = Props(new RenameFolderHandler(session))
}

class RenameFolderHandler(session: ImapAsyncSession) extends BaseActor {
  override def receive: Receive = {
    case Command.RenameFolder(userId, oldFolder, newFolder) =>
      logger.trace(s"RenameFolderHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitCallback(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import scala.jdk.CollectionConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.toSeq)
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.RenameFolderResponse(responsesList)
      }
      val errorCallback: Consumer[Exception] = e => {
        logger.trace(s"${getClass.getSimpleName} command failed", e)
        logger.error(s"${getClass.getSimpleName} command failed")
        sender ! e
        context.stop(self)
      }

      val future = session.execute(new RenameFolderCommand(oldFolder, newFolder))
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.RenameFolderResponse(_) =>
      logger.trace(s"RenameFolderHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }
}
