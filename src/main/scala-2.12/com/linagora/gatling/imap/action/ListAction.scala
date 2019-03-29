package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.{Command, UserId}
import io.gatling.commons.validation.Validation
import io.gatling.core.action.ValidatedActionActor
import io.gatling.core.session._

import scala.collection.immutable.Seq

object ListAction {
  def props(imapContext: ImapActionContext, requestname: String, checks: Seq[ImapCheck], reference: Expression[String], name: Expression[String]) =
    Props(new ListAction(imapContext, requestname, checks, reference, name))
}

class ListAction(val imapContext: ImapActionContext, val requestName: String, override val checks: Seq[ImapCheck], reference: Expression[String], name: Expression[String]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      reference <- reference(session)
      name <- name(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.List(UserId(id), reference, name), handler)
    }
  }
}