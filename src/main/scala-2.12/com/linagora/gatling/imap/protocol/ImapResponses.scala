package com.linagora.gatling.imap.protocol

import java.util.regex.Pattern

import com.sun.mail.imap.protocol.IMAPResponse

import scala.collection.immutable.Seq

case class ImapResponses(responses: Seq[IMAPResponse]) {

  import ImapResponses._

  def mkString(separator: String = ",") = {
    responses.mkString(separator)
  }

  def isBad = responses.lastOption.exists(_.isBAD)

  def isOk = responses.lastOption.exists(_.isOK)

  def isNo = responses.lastOption.exists(_.isNO)

  def countRecent: Option[Int] = {
    responses.map(_.toString).find(_.matches(Recent.regex))
      .map {
        case Recent(actual) => actual.toInt
      }
  }

  def folderList: Seq[String] = {
    responses.map(_.toString).filter(_.matches(List.regex))
      .map {
        case List(name, null) => name
        case List(null, quotedName) => quotedName
      }
  }

  def uidList: Seq[Uid] = {
    responses.map(_.toString).filter(_.matches(UidRegex.regex))
      .map {
        case UidRegex(uid) => Uid(uid.toInt)
      }
  }

  def contains(content: String): Boolean =
    responses.map(_.toString).exists(_.contains(content))
}

object ImapResponses {
  val empty = ImapResponses(Seq.empty)

  private[this] val dotAllFlag = """(?s)"""
  private[this] val startWithStar = """^(?:(?:, )?\*)"""
  private[this] val mailboxName = """(?:"([^"]*)"|([^"\s]*))"""

  private val Recent = (dotAllFlag + startWithStar + """ (\d+) RECENT\s*$""").r
  private val List = ("""^\* LIST .*? """ + mailboxName + """\s*$""").r
  private val UidRegex = (dotAllFlag + startWithStar + """ .*UID (\d+).*$""").r
}
