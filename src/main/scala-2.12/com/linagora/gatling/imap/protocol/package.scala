package com.linagora.gatling.imap

import java.util.Calendar

import com.linagora.gatling.imap.protocol.command.{FetchAttributes, MessageRanges, StoreFlags}

import scala.collection.immutable.Seq

package object protocol {

  case class User(login: String, password: String)

  trait Command {
    def userId: UserId
  }

  object Command {

    case class Connect(userId: UserId) extends Command

    case class Login(userId: UserId, username: String, password: String) extends Command

    object Login {
      def apply(userId: UserId, user: User): Login = new Login(userId, user.login, user.password)
    }

    case class Select(userId: UserId, mailbox: String) extends Command

    case class List(userId: UserId, reference: String, mailbox: String) extends Command

    case class Fetch(userId: UserId, sequence: MessageRanges, attributes: FetchAttributes) extends Command

    case class UIDFetch(userId: UserId, sequence: MessageRanges, attributes: FetchAttributes) extends Command

    case class Expunge(userId: UserId) extends Command

    case class Append(userId: UserId, mailbox: String, flags: Option[Seq[String]], date: Option[Calendar], content: String) extends Command

    case class Disconnect(userId: UserId) extends Command

    case class Store(userId: UserId, sequence: MessageRanges, flags: StoreFlags) extends Command
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

