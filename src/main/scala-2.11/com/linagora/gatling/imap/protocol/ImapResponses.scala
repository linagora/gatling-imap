package com.linagora.gatling.imap.protocol

import com.sun.mail.imap.protocol.IMAPResponse

import scala.collection.immutable.Seq

case class ImapResponses(responses: Seq[IMAPResponse]) {

  import ImapResponses._

  def mkString(separator: String = ",") = {
    responses.mkString(separator)
  }

  def isBad = responses.lastOption.exists(_.isBAD)

  def isOk = responses.lastOption.exists(_.isOK)

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
}

object ImapResponses {
  val empty = ImapResponses(Seq.empty)
  private val Recent = """^\* (\d+) RECENT$""".r
  private val List = """^^\* LIST .* (?:([^ "]+)|"(.*)")$""".r
}
