package com.linagora.gatling.imap.protocol.command

import javax.mail.Flags

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.{FlagsAction, StoreFlagsCommand}
import io.gatling.core.akka.BaseActor

import scala.collection.immutable.Seq

sealed abstract class StoreFlags(prefix: String) {
  def flags: Seq[String]

  def silent: Silent

  val silentAsString: String = if (silent.enable) ".SILENT" else ""
  val flagsAsString: String = flags.mkString("(", " ", ")")

  def asString: String = s"${prefix}${silentAsString} ${flagsAsString}"

  def setFlags(flags: Seq[String]): StoreFlags

  def asImap: Flags = {
    val imapFlags = new Flags()
    flags.foreach(imapFlags.add)
    imapFlags
  }

  def action: FlagsAction
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

    override def action: FlagsAction = FlagsAction.REPLACE
  }

  case class FlagAdd(silent: Silent, flags: Seq[String]) extends StoreFlags("+FLAGS") {
    override def setFlags(flags: Seq[String]): FlagAdd = FlagAdd(silent = silent, flags = flags)

    override def action: FlagsAction = FlagsAction.ADD
  }

  case class FlagRemove(silent: Silent, flags: Seq[String]) extends StoreFlags("-FLAGS") {
    override def setFlags(flags: Seq[String]): FlagRemove = FlagRemove(silent = silent, flags = flags)

    override def action: FlagsAction = FlagsAction.REMOVE
  }

}

object StoreHandler {
  def props(session: ImapAsyncSession) = Props(new StoreHandler(session))
}

class StoreHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.Store(userId, sequence, flags) =>
      context.become(waitCallback(sender()))
      ImapSessionExecutor.listen(self, userId, Response.Stored)(logger)(session.execute(new StoreFlagsCommand(sequence.asImap, flags.asImap, flags.action, true)))
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Stored(_) =>
      sender ! msg
      context.stop(self)
  }

}
