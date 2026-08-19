package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.command.MessageRanges
import com.yahoo.imapnio.async.request.UidExpungeCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

class UidExpungeAction(imapContext: ImapActionContext,
                       requestName: String,
                       checks: Seq[ImapCheck],
                       sequence: Expression[MessageRanges]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      sequence <- sequence(session)
      s <- sessionFor(session)
    } yield {
      val future = s.execute(new UidExpungeCommand(sequence.asImap))
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}