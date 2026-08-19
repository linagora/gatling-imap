package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.command.SetQuotaCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

class SetQuotaAction(imapContext: ImapActionContext,
                     requestName: String,
                     checks: Seq[ImapCheck],
                     quotaRootAndResourceLimits: Expression[String]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      quota <- quotaRootAndResourceLimits(session)
      s <- sessionFor(session)
    } yield {
      val future = s.execute(new SetQuotaCommand(quota))
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}