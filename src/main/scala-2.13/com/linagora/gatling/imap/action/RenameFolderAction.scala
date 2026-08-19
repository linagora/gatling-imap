package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.yahoo.imapnio.async.request.RenameFolderCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

class RenameFolderAction(imapContext: ImapActionContext,
                         requestName: String,
                         checks: Seq[ImapCheck],
                         oldFolder: Expression[String],
                         newFolder: Expression[String]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      oldFolder <- oldFolder(session)
      newFolder <- newFolder(session)
      s <- sessionFor(session)
    } yield {
      val future = s.execute(new RenameFolderCommand(oldFolder, newFolder))
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}