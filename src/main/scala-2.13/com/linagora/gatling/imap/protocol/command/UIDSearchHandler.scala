package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.{SearchCommand, UidSearchCommand}
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

object UIDSearchHandler {
  def props(session: ImapAsyncSession) = Props(new UIDSearchHandler(session))
}

class UIDSearchHandler(session: ImapAsyncSession) extends BaseActor {
  override def receive: Receive = {
    case Command.UIDSearch(userId, range, term) =>
      logger.trace(s"UidSearchHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitForLoggedIn(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import scala.jdk.CollectionConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.toSeq)
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.UIDSearchResult(responsesList)
      }

      val errorCallback: Consumer[Exception] = e => {
        logger.trace(s"${getClass.getSimpleName} command failed", e)
        logger.error(s"${getClass.getSimpleName} command failed")
        sender ! e
        context.stop(self)
      }

      val future = session.execute(new UidSearchCommand(range.asImap, term, null))
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@Response.UIDSearchResult(_) =>
      logger.trace(s"UidSearchHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }
}

