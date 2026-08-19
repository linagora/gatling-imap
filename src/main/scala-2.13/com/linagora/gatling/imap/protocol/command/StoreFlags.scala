package com.linagora.gatling.imap.protocol.command

import com.yahoo.imapnio.async.request.FlagsAction
import javax.mail.Flags

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