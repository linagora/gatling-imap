package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.command.GetQuotaCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

class GetQuotaAction(imapContext: ImapActionContext,
                     requestName: String,
                     checks: Seq[ImapCheck],
                     quotaRoot: Expression[String]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      quotaRoot <- quotaRoot(session)
      s <- sessionFor(session)
    } yield {
      val future = s.execute(new GetQuotaCommand(quotaRoot))
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}