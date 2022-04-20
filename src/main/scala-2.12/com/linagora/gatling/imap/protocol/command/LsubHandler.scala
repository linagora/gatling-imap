package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.LSubCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

object LsubHandler {
  def props(session: ImapAsyncSession) = Props(new LsubHandler(session))
}

class LsubHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.Lsub(userId, reference, name) =>
      context.become(waitCallback(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import collection.JavaConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.to[Seq])
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.Lsubed(responsesList)}

      val future = session.execute(new LSubCommand(reference, name))
      future.setDoneCallback(responseCallback)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Lsubed(_) =>
      sender ! msg
      context.stop(self)
  }

}
