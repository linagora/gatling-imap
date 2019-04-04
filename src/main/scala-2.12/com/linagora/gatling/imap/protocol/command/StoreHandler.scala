package com.linagora.gatling.imap.protocol.command

import java.util

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.linagora.gatling.imap.protocol.{Command, ImapResponses, Response, Tag, UserId}
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor

import collection.immutable.Seq

sealed abstract class StoreFlags(prefix: String) {
  def flags: Seq[String]
  def silent: Silent

  val silentAsString: String = if (silent.enable) ".SILENT" else ""
  val flagsAsString: String  = flags.mkString("(", " ", ")")

  def asString: String = s"${prefix}${silentAsString} ${flagsAsString}"

  def setFlags(flags: Seq[String]): StoreFlags
}

abstract class Silent(val enable: Boolean) { }

object Silent {
  case class Enable() extends Silent(true)

  case class Disable() extends Silent(false)
}

object StoreFlags {

  def replace(silent: Silent, flags: String*): FlagReplace = FlagReplace(silent, Seq(flags:_*))
  def add(silent: Silent, flags: String*): FlagReplace = FlagReplace(silent, Seq(flags:_*))
  def remove(silent: Silent, flags: String*): FlagReplace = FlagReplace(silent, Seq(flags:_*))

  case class FlagReplace(silent: Silent, flags: Seq[String]) extends StoreFlags("FLAGS") {
    override def setFlags(flags: Seq[String]): FlagReplace = FlagReplace(silent = silent, flags = flags)
  }

  case class FlagAdd(silent: Silent, flags: Seq[String]) extends StoreFlags("+FLAGS") {
    override def setFlags(flags: Seq[String]): FlagAdd = FlagAdd(silent = silent, flags = flags)
  }

  case class FlagRemove(silent: Silent, flags: Seq[String]) extends StoreFlags("-FLAGS") {
    override def setFlags(flags: Seq[String]): FlagRemove = FlagRemove(silent = silent, flags = flags)
  }
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


  class StoreListener(userId: UserId) extends IMAPCommandListener {

    import collection.JavaConverters._

    override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
      logger.trace(s"Untagged message for $userId : ${response.toString}")
    }

    override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
      val response = ImapResponses(responses.asScala.to[collection.immutable.Seq])
      logger.trace(s"On response for $userId :\n ${response.mkString("\n")}\n ${sender.path}")
      self ! Response.Stored(response)
    }
  }

}
