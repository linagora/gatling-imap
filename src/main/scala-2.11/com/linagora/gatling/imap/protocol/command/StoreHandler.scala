package com.linagora.gatling.imap.protocol.command

import java.util

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.linagora.gatling.imap.protocol.{Command, ImapResponses, Response, Tag}
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

sealed abstract class StoreFlags(prefix: String, silent: Boolean, flags: String*) {
  val silentAsString: String = if (silent) ".SILENT" else ""
  val flagsAsString: String  = flags.mkString("(", " ", ")")

  def asString: String = s"${prefix}${silentAsString} ${flagsAsString}"
}

object StoreFlags {
  case class FlagReplace(silent: Boolean, flags: String*) extends StoreFlags("FLAGS", silent, flags:_*)

  case class FlagAdd(silent: Boolean, flags: String*) extends StoreFlags("+FLAGS", silent, flags:_*)

  case class FlagRemove(silent: Boolean, flags: String*) extends StoreFlags("-FLAGS", silent, flags:_*)
}

object StoreHandler {
  def props(session: IMAPSession, tag: Tag) = Props(new StoreHandler(session, tag))
}

class StoreHandler(session: IMAPSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.Store(userId, sequence, flags) =>
      val listener = new StoreListener(userId)

      session.executeTaggedRawTextCommand(tag.string, s"STORE ${sequence.asString} ${flags.asString}", listener)
      context.become(waitCallback(sender()))
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Stored(response) =>
      sender ! msg
      context.stop(self)
  }


  class StoreListener(userId: String) extends IMAPCommandListener {

    import collection.JavaConverters._

    override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
      logger.trace(s"Untagged message for $userId : ${response.toString}")
    }

    override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
      val response = ImapResponses(responses.asScala.to[Seq])
      logger.trace(s"On response for $userId :\n ${response.mkString("\n")}\n ${sender.path}")
      self ! Response.Stored(response)
    }
  }

}
