package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.command.{MessageRange, MessageRanges}
import com.linagora.gatling.imap.protocol.{Command, UserId}
import io.gatling.commons.validation.Validation
import io.gatling.core.session._
import javax.mail.search.SearchTerm

import scala.collection.immutable.Seq

object SearchAction {
  def props(imapContext: ImapActionContext, requestName: String, checks: Seq[ImapCheck], sequence: Expression[MessageRanges], searchTerm: Expression[SearchTerm]) =
    Props(new SearchAction(imapContext, requestName, checks, sequence, searchTerm))
}

class SearchAction(val imapContext: ImapActionContext,
                   val requestName: String,
                   override val checks: Seq[ImapCheck],
                   sequence: Expression[MessageRanges],
                   searchTerm: Expression[SearchTerm]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      sequence <- sequence(session)
      searchTerm <- searchTerm(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.Search(UserId(id), sequence, searchTerm), handler)
    }
  }
}
