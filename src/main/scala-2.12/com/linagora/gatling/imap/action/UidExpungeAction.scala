package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.command.MessageRanges
import com.linagora.gatling.imap.protocol.{Command, UserId}
import io.gatling.commons.validation.Validation
import io.gatling.core.action.ValidatedActionActor
import io.gatling.core.session._

import scala.collection.immutable.Seq

object UidExpungeAction {
  def props(imapContext: ImapActionContext, requestName: String, checks: Seq[ImapCheck], sequence: Expression[MessageRanges]) =
    Props(new UidExpungeAction(imapContext, requestName, checks, sequence))
}

class UidExpungeAction(val imapContext: ImapActionContext,
                       val requestName: String,
                       override val checks: Seq[ImapCheck],
                       sequence: Expression[MessageRanges]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      sequence <- sequence(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.UidExpunge(UserId(id), sequence), handler)
    }
  }
}