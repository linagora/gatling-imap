package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.protocol.{ImapResponses, UserId}
import io.gatling.commons.validation.Success
import io.gatling.core.session.Session

class ConnectAction(imapContext: ImapActionContext, requestName: String) extends ImapRequestAction(imapContext, requestName, Seq.empty) {
  override def sendRequest(session: Session): io.gatling.commons.validation.Validation[Unit] = {
    val start = clock.nowMillis
    components.connect(UserId(session.userId))(
      _ => handleResponse(session, start)(ImapResponses.empty),
      e => handleError(session, start)(e))
    Success(())
  }
}