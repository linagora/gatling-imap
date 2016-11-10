package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.Command
import io.gatling.commons.util.TimeHelper._
import io.gatling.commons.validation.Validation
import io.gatling.core.action.ValidatedActionActor
import io.gatling.core.session._

import scala.collection.immutable.Seq

object SelectAction {
  def props(imapContext: ImapActionContext, requestname: String, checks: Seq[ImapCheck], mailbox: Expression[String]) =
    Props(new SelectAction(imapContext, requestname, checks, mailbox))
}

class SelectAction(val imapContext: ImapActionContext, val requestName: String, override val checks: Seq[ImapCheck], mailbox: Expression[String]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      mailbox <- mailbox(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, nowMillis)
      sessions.tell(Command.Select(id.toString, mailbox), handler)
    }
  }
}