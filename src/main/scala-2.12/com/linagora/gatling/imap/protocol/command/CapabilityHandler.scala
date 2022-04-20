package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.CapaCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

object CapabilityHandler {
  def props(session: ImapAsyncSession) = Props(new CapabilityHandler(session))
}

class CapabilityHandler(session: ImapAsyncSession) extends BaseActor {
  override def receive: Receive = {
    case Command.Capability(userId) =>
      logger.trace(s"CapabilityHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitForLoggedIn(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import collection.JavaConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.to[Seq])
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.Capabilities(responsesList)}
      val errorCallback: Consumer[Exception] = e => {
        logger.error("CapabilityHandler command failed", e)
      }

      val future = session.execute(new CapaCommand())
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@Response.Capabilities(_) =>
      logger.trace(s"CapabilityHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }
}

