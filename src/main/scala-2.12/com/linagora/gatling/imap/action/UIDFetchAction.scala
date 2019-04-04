package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.{Command, UserId}
import com.linagora.gatling.imap.protocol.command.{FetchAttributes, MessageRanges}
import io.gatling.commons.validation.Validation
import io.gatling.core.action.ValidatedActionActor
import io.gatling.core.session._

import scala.collection.immutable.Seq

object UIDFetchAction {
  def props(imapContext: ImapActionContext, requestname: String, checks: Seq[ImapCheck], sequence: Expression[MessageRanges], attributes: Expression[FetchAttributes]) =
    Props(new UIDFetchAction(imapContext, requestname, checks, sequence, attributes))
}

class UIDFetchAction(val imapContext: ImapActionContext, val requestName: String, override val checks: Seq[ImapCheck], sequence: Expression[MessageRanges], attributes: Expression[FetchAttributes]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      sequence <- sequence(session)
      attributes <- attributes(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.UIDFetch(UserId(id), sequence, attributes), handler)
    }
  }
}