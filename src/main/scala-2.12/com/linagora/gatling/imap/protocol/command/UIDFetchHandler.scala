package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.{SelectFolderCommand, UidFetchCommand}
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

object UIDFetchHandler {
  def props(session: ImapAsyncSession) = Props(new UIDFetchHandler(session))
}

class UIDFetchHandler(session: ImapAsyncSession) extends BaseActor {
  override def receive: Receive = {
    case Command.UIDFetch(userId, sequence, attributes) =>
      context.become(waitCallback(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import collection.JavaConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.to[Seq])
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.Fetched(responsesList)}
      val errorCallback: Consumer[Exception] = _ => {}

      val future = session.execute(new UidFetchCommand(sequence.asImap, attributes.asString))
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Fetched(_) =>
      sender ! msg
      context.stop(self)
  }
}
