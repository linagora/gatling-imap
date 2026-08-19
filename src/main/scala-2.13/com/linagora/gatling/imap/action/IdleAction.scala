package com.linagora.gatling.imap.action

import java.util.concurrent.ConcurrentLinkedQueue

import com.linagora.gatling.imap.check.ImapCheck
import com.sun.mail.imap.protocol.IMAPResponse
import com.yahoo.imapnio.async.request.IdleCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session.Session

import scala.collection.immutable.Seq

class IdleAction(imapContext: ImapActionContext,
                 requestName: String,
                 checks: Seq[ImapCheck]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      s <- sessionFor(session)
    } yield {
      val idleCommand = new IdleCommand(new ConcurrentLinkedQueue[IMAPResponse]())
      s.execute(idleCommand).setExceptionCallback(e => handleError(session, start)(e))
      val terminationFuture = s.terminateCommand(idleCommand)
      terminationFuture.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      terminationFuture.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}