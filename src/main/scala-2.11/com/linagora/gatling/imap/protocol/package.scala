package com.linagora.gatling.imap

package object protocol {

  trait Command {
    def userId: String
  }

  object Command {

    case class Connect(userId: String) extends Command

    case class Login(userId: String, username: String, password: String) extends Command

    case class Select(userId: String, mailbox: String) extends Command

    case class Disconnect(userId: String) extends Command

  }

  sealed trait Response {
    def responses: ImapResponses
  }

  object Response {
    def unapply(arg: Response): Option[ImapResponses] = Some(arg.responses)

    case class Connected(responses: ImapResponses) extends Response

    case class LoggedIn(responses: ImapResponses) extends Response

    case class Selected(responses: ImapResponses) extends Response

    case class Disconnected(cause: Throwable)

  }

}

