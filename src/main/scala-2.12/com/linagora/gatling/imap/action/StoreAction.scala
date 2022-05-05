package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.{Command, UserId}
import com.linagora.gatling.imap.protocol.command.{MessageRanges, StoreFlags}
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

object StoreAction {
  def props(imapContext: ImapActionContext, requestname: String, checks: Seq[ImapCheck], sequence: Expression[MessageRanges], flags: Expression[StoreFlags]) =
    Props(new StoreAction(imapContext, requestname, checks, sequence, flags))
}

class StoreAction(val imapContext: ImapActionContext, val requestName: String, override val checks: Seq[ImapCheck], sequence: Expression[MessageRanges], flags: Expression[StoreFlags]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      sequence <- sequence(session)
      flags <- flags(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.Store(UserId(id), sequence, flags), handler)
    }
  }
}
