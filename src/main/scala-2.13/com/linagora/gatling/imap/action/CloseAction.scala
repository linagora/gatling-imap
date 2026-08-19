package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.yahoo.imapnio.async.request.CloseCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session.Session

import scala.collection.immutable.Seq

class CloseAction(imapContext: ImapActionContext,
                  requestName: String,
                  checks: Seq[ImapCheck]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      s <- sessionFor(session)
    } yield {
      val future = s.execute(new CloseCommand())
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}