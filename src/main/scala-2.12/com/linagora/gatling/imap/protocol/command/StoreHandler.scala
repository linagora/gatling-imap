package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.linagora.gatling.imap.protocol._
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

sealed abstract class StoreFlags(prefix: String) {
  def flags: Seq[String]

  def silent: Silent

  val silentAsString: String = if (silent.enable) ".SILENT" else ""
  val flagsAsString: String = flags.mkString("(", " ", ")")

  def asString: String = s"${prefix}${silentAsString} ${flagsAsString}"

  def setFlags(flags: Seq[String]): StoreFlags
}

abstract class Silent(val enable: Boolean) {}

object Silent {

  case class Enable() extends Silent(true)

  case class Disable() extends Silent(false)

}

object StoreFlags {

  def replace(silent: Silent, flags: String*): FlagReplace = FlagReplace(silent, Seq(flags: _*))

  def add(silent: Silent, flags: String*): FlagReplace = FlagReplace(silent, Seq(flags: _*))

  def remove(silent: Silent, flags: String*): FlagReplace = FlagReplace(silent, Seq(flags: _*))

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
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Stored)(logger)
      context.become(waitCallback(sender()))
      session.executeTaggedRawTextCommand(tag.string, s"STORE ${sequence.asString} ${flags.asString}", listener)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Stored(response) =>
      sender ! msg
      context.stop(self)
  }

}
