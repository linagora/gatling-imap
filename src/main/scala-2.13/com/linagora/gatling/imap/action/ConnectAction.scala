package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.protocol.{Command, UserId}
import io.gatling.core.session.Session

object ConnectAction {
  def props(imapContext: ImapActionContext, requestName: String): Props =
    Props(new ConnectAction(imapContext, requestName))
}

class ConnectAction(val imapContext: ImapActionContext, val requestName: String) extends ActionActor with ImapActionActor {
  override def execute(session: Session): Unit = {
    sessions.tell(Command.Connect(UserId(session.userId)), handleResponse(session, imapContext.clock.nowMillis))
  }

}
