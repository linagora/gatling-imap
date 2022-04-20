package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.StatusCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

object StatusHandler {
  def props(session: ImapAsyncSession) = Props(new StatusHandler(session))
}

class StatusHandler(session: ImapAsyncSession) extends BaseActor {
  override def receive: Receive = {
    case Command.Status(userId, mailbox, items) =>
      context.become(waitCallback(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import collection.JavaConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.to[Seq])
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.Status(responsesList)}
      val errorCallback: Consumer[Exception] = _ => {}

      val itemsAsString = new Array[String](items.items.size)
      items.items.map(_.asString).copyToArray[String](itemsAsString)
      val future = session.execute(new StatusCommand(mailbox, itemsAsString))
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Status(_) =>
      sender ! msg
      context.stop(self)
  }
}
