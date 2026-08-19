package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.yahoo.imapnio.async.request.ListCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

class ListAction(imapContext: ImapActionContext,
                 requestName: String,
                 checks: Seq[ImapCheck],
                 reference: Expression[String],
                 name: Expression[String]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      reference <- reference(session)
      name <- name(session)
      s <- sessionFor(session)
    } yield {
      val future = s.execute(new ListCommand(reference, name))
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}