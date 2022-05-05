package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.{Command, UserId}
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

object GetQuotaAction {
  def props(imapContext: ImapActionContext, requestname: String, checks: Seq[ImapCheck], quotaRoot: Expression[String]) =
    Props(new GetQuotaAction(imapContext, requestname, checks, quotaRoot))
}

class GetQuotaAction(val imapContext: ImapActionContext, val requestName: String, override val checks: Seq[ImapCheck], quotaRoot: Expression[String]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      quotaRoot <- quotaRoot(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.GetQuota(UserId(id), quotaRoot), handler)
    }
  }
}