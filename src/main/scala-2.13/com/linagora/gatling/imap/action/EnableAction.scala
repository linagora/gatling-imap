package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.yahoo.imapnio.async.request.EnableCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

class EnableAction(imapContext: ImapActionContext,
                   requestName: String,
                   checks: Seq[ImapCheck],
                   capability: Expression[String]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      capability <- capability(session)
      s <- sessionFor(session)
    } yield {
      val future = s.execute(new EnableCommand(Array(capability)))
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}