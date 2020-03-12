package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.ListCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import io.gatling.core.akka.BaseActor

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object ListHandler {
  def props(session: ImapAsyncSession) = Props(new ListHandler(session))
}

class ListHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.List(userId, reference, name) =>
      context.become(waitCallback(sender()))
      ImapSessionExecutor.listenWithHandler(self, userId, Response.Listed, callback)(logger)(session.execute(new ListCommand(reference, name)))
  }

  private def callback(response: Future[ImapAsyncResponse]) = {
    Try(response) match {
      case Success(_) =>
      case Failure(e) =>
        logger.error("ERROR when executing LIST COMMAND", e)
        throw e;
    }
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Listed(_) =>
      sender ! msg
      context.stop(self)
  }

}
