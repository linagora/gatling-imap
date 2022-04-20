package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.UnselectCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

object UnselectHandler {
  def props(session: ImapAsyncSession) = Props(new UnselectHandler(session))
}

class UnselectHandler(session: ImapAsyncSession) extends BaseActor {
  override def receive: Receive = {
    case Command.Unselect(userId) =>
      logger.trace(s"UnselectHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitForLoggedIn(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import collection.JavaConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.to[Seq])
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.UnSelected(responsesList)}
      val errorCallback: Consumer[Exception] = e => {
        logger.error("UnselectHandler command failed", e)
      }

      val future = session.execute(new UnselectCommand())
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@Response.UnSelected(_) =>
      logger.trace(s"UnselectHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }
}

