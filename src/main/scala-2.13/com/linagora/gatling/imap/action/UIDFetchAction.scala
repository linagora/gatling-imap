package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.command.{FetchAttributes, MessageRanges}
import com.yahoo.imapnio.async.request.UidFetchCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

class UIDFetchAction(imapContext: ImapActionContext,
                     requestName: String,
                     checks: Seq[ImapCheck],
                     sequence: Expression[MessageRanges],
                     attributes: Expression[FetchAttributes]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      sequence <- sequence(session)
      attributes <- attributes(session)
      s <- sessionFor(session)
    } yield {
      val future = s.execute(new UidFetchCommand(sequence.asImap, attributes.asString))
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}