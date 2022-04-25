package com.linagora.gatling.imap.protocol.command

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.sun.mail.imap.protocol.IMAPResponse
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.IdleCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

object IdleHandler {
  def props(session: ImapAsyncSession) = Props(new IdleHandler(session))
}

class IdleHandler(session: ImapAsyncSession) extends BaseActor {
  override def receive: Receive = {
    case Command.Idle(userId) =>
      logger.trace(s"IdleHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitForLoggedIn(sender()))
      val idleCommand = new IdleCommand(new ConcurrentLinkedQueue[IMAPResponse]())

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import collection.JavaConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.to[Seq])
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.Idled(responsesList)
      }

      val errorCallback: Consumer[Exception] = e => {
        logger.trace(s"${getClass.getSimpleName} command failed", e)
        logger.error(s"${getClass.getSimpleName} command failed")
        sender ! e
        context.stop(self)
      }

      val future = session.execute(idleCommand)
      future.setExceptionCallback(errorCallback)
      val terminationFuture = session.terminateCommand(idleCommand)
      terminationFuture.setDoneCallback(responseCallback)
      terminationFuture.setExceptionCallback(errorCallback)
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@Response.Idled(_) =>
      logger.trace(s"IdleHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }
}

