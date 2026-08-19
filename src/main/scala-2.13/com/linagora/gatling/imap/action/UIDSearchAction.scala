package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.command.MessageRanges
import com.yahoo.imapnio.async.request.UidSearchCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session._
import javax.mail.search.SearchTerm

import scala.collection.immutable.Seq

class UIDSearchAction(imapContext: ImapActionContext,
                      requestName: String,
                      checks: Seq[ImapCheck],
                      sequence: Expression[MessageRanges],
                      searchTerm: Expression[SearchTerm]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      sequence <- sequence(session)
      searchTerm <- searchTerm(session)
      s <- sessionFor(session)
    } yield {
      val future = s.execute(new UidSearchCommand(sequence.asImap, searchTerm, null))
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}