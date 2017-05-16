package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.Command
import io.gatling.commons.util.TimeHelper._
import io.gatling.commons.validation.Validation
import io.gatling.core.action.ValidatedActionActor
import io.gatling.core.session._

import scala.collection.immutable.Seq

object ExpungeAction {
  def props(imapContext: ImapActionContext, requestname: String, checks: Seq[ImapCheck]) =
    Props(new ExpungeAction(imapContext, requestname, checks))
}

class ExpungeAction(val imapContext: ImapActionContext, val requestName: String, override val checks: Seq[ImapCheck]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    val id: Long = session.userId
    val handler = handleResponse(session, nowMillis)
    sessions.tell(Command.Expunge(id.toString), handler)

    StaticStringExpression("null")(session)
  }
}
