package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.action.BaseActor
import com.linagora.gatling.imap.protocol.{Command, ImapResponses, Response}
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.UidStoreFlagsCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse

object UidStoreHandler {
  def props(session: ImapAsyncSession) = Props(new UidStoreHandler(session))
}

class UidStoreHandler(session: ImapAsyncSession) extends BaseActor {
  override def receive: Receive = {
    case Command.UidStore(userId, sequence, flags) =>
      logger.trace(s"UidStoreHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitCallback(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import scala.jdk.CollectionConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.toSeq)
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.UidStoreResult(responsesList)
      }

      val errorCallback: Consumer[Exception] = e => {
        logger.trace(s"${getClass.getSimpleName} command failed", e)
        logger.error(s"${getClass.getSimpleName} command failed")
        sender ! e
        context.stop(self)
      }

      val future = session.execute(new UidStoreFlagsCommand(sequence.asImap, flags.asImap, flags.action, true))
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.UidStoreResult(_) =>
      logger.trace(s"UidStoreHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }
}