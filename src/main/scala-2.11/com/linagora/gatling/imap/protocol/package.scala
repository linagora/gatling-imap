package com.linagora.gatling.imap

import java.util.Calendar

import com.linagora.gatling.imap.protocol.command.{FetchAttributes, MessageRange, StoreFlags}

import scala.collection.immutable.Seq

package object protocol {

  trait Command {
    def userId: String
  }

  object Command {

    case class Connect(userId: String) extends Command

    case class Login(userId: String, username: String, password: String) extends Command

    case class Select(userId: String, mailbox: String) extends Command

    case class List(userId: String, reference: String, mailbox: String) extends Command

    case class Fetch(userId: String, sequence: Seq[MessageRange], attributes: FetchAttributes) extends Command

    case class Expunge(userId: String) extends Command

    case class Append(userId: String, mailbox: String, flags: Option[Seq[String]], date: Option[Calendar], content: String) extends Command

    case class Disconnect(userId: String) extends Command

    case class Store(userId: String, sequence: Seq[MessageRange], flags: StoreFlags) extends Command

  }

  sealed trait Response {
    def responses: ImapResponses
  }

  object Response {
    def unapply(arg: Response): Option[ImapResponses] = Some(arg.responses)

    case class Connected(responses: ImapResponses) extends Response

    case class LoggedIn(responses: ImapResponses) extends Response

    case class Selected(responses: ImapResponses) extends Response

    case class Listed(responses: ImapResponses) extends Response

    case class Fetched(responses: ImapResponses) extends Response

    case class Expunged(responses: ImapResponses) extends Response

    case class Stored(responses: ImapResponses) extends Response

    case class Appended(responses: ImapResponses) extends Response

    case class Disconnected(cause: Throwable)

  }

}

