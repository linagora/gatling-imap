package com.linagora.gatling.imap.protocol.command

import java.util.function.Consumer

import javax.mail.Flags
import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.{FlagsAction, StoreFlagsCommand, UidFetchCommand}
import com.yahoo.imapnio.async.response.ImapAsyncResponse
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

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import scala.jdk.CollectionConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.toSeq)
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
        self !  Response.Stored(responsesList)
      }

      val errorCallback: Consumer[Exception] = e => {
        logger.trace(s"${getClass.getSimpleName} command failed", e)
        logger.error(s"${getClass.getSimpleName} command failed")
        sender ! e
        context.stop(self)
      }

      val future = session.execute(new StoreFlagsCommand(sequence.asImap, flags.asImap, flags.action, true))
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Stored(_) =>
      sender ! msg
      context.stop(self)
  }
}
