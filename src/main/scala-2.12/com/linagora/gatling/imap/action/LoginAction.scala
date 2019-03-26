package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.Command
import io.gatling.commons.validation.Validation
import io.gatling.core.action.ValidatedActionActor
import io.gatling.core.session._

import scala.collection.immutable.Seq

object LoginAction {
  def props(imapContext: ImapActionContext, requestName: String, checks: Seq[ImapCheck], username: Expression[String], password: Expression[String]) =
    Props(new LoginAction(imapContext, requestName, checks, username, password))
}

class LoginAction(val imapContext: ImapActionContext,
                  val requestName: String,
                  override val checks: Seq[ImapCheck],
                  username: Expression[String],
                  password: Expression[String]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      user <- username(session)
      pass <- password(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.Login(id.toString, user, pass), handler)
    }
  }
}
