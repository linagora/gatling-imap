package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.protocol.{Command, UserId}
import io.gatling.core.action.ActionActor
import io.gatling.core.session.Session

object DisconnectAction {
  def props(imapContext: ImapActionContext, requestName: String): Props =
    Props(new DisconnectAction(imapContext, requestName))
}

class DisconnectAction(val imapContext: ImapActionContext, val requestName: String) extends ActionActor with ImapActionActor {


  override def successOnDisconnect: Boolean = true

  override def execute(session: Session): Unit = {
    sessions.tell(Command.Disconnect(UserId(session.userId)), handleResponse(session, imapContext.clock.nowMillis))
  }
}
