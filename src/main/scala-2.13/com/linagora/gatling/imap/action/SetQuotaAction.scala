package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.{Command, UserId}
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

object SetQuotaAction {
  def props(imapContext: ImapActionContext, requestname: String, checks: Seq[ImapCheck], quotaRootAndResourceLimits: Expression[String]) =
    Props(new SetQuotaAction(imapContext, requestname, checks, quotaRootAndResourceLimits))
}

class SetQuotaAction(val imapContext: ImapActionContext, val requestName: String, override val checks: Seq[ImapCheck], quotaRootAndResourceLimits: Expression[String]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      quotaRootAndResourceLimits <- quotaRootAndResourceLimits(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.SetQuota(UserId(id), quotaRootAndResourceLimits), handler)
    }
  }
}