package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.{Command, UserId}
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

object IdleAction {
  def props(imapContext: ImapActionContext, requestName: String, checks: Seq[ImapCheck]) =
    Props(new IdleAction(imapContext, requestName, checks))
}

class IdleAction(val imapContext: ImapActionContext,
                 val requestName: String,
                 override val checks: Seq[ImapCheck]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    Validation.unit
      .map(_ => {
        val id: Long = session.userId
        val handler = handleResponse(session, imapContext.clock.nowMillis)
        sessions.tell(Command.Idle(UserId(id)), handler)
      })
  }
}
