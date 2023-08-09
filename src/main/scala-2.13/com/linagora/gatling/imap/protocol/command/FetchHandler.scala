package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.action.BaseActor
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.FetchCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse


abstract class FetchAttributes {
  def asString: String
}

object FetchAttributes {

  case class ALL() extends FetchAttributes {
    override def asString = "ALL"
  }

  case class FULL() extends FetchAttributes {
    override def asString = "FULL"
  }

  case class FAST() extends FetchAttributes {
    override def asString = "FAST"
  }

  case class AttributeList(fetchAttributes: String*) extends FetchAttributes {
    override def asString = fetchAttributes.mkString(" ")
  }

}

object FetchHandler {
  def props(session: ImapAsyncSession) = Props(new FetchHandler(session))
}

class FetchHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.Fetch(userId, sequence, attributes) =>
      context.become(waitCallback(sender()))

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import scala.jdk.CollectionConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.toSeq)
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self ! Response.Fetched(responsesList)
      }

      val errorCallback: Consumer[Exception] = e => {
        logger.trace(s"${getClass.getSimpleName} command failed", e)
        logger.error(s"${getClass.getSimpleName} command failed")
        sender ! e
        context.stop(self)
      }

      val future = session.execute(new FetchCommand(sequence.asImap, attributes.asString))
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Fetched(_) =>
      sender ! msg
      context.stop(self)
  }

}
