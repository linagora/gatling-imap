package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.StatusItems
import com.yahoo.imapnio.async.request.StatusCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

class StatusAction(imapContext: ImapActionContext,
                   requestName: String,
                   checks: Seq[ImapCheck],
                   mailbox: Expression[String],
                   items: Expression[StatusItems]) extends ImapRequestAction(imapContext, requestName, checks) {

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      mailbox <- mailbox(session)
      items <- items(session)
      s <- sessionFor(session)
    } yield {
      val itemsAsString = new Array[String](items.items.size)
      items.items.map(_.asString).copyToArray[String](itemsAsString)
      val future = s.execute(new StatusCommand(mailbox, itemsAsString))
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }
}