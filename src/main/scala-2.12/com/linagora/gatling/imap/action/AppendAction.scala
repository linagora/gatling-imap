package com.linagora.gatling.imap.action

import java.util.Calendar

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.Command
import io.gatling.commons.validation.Validation
import io.gatling.core.action.ValidatedActionActor
import io.gatling.core.session._

import scala.collection.immutable.Seq

object AppendAction {
  def props(imapContext: ImapActionContext, requestname: String, checks: Seq[ImapCheck], mailbox: Expression[String], flags: Expression[Option[Seq[String]]], date: Expression[Option[Calendar]], content: Expression[String]) =
    Props(new AppendAction(imapContext, requestname, checks, mailbox, flags, date, content))
}

class AppendAction(val imapContext: ImapActionContext, val requestName: String, override val checks: Seq[ImapCheck], mailbox: Expression[String], flags: Expression[Option[Seq[String]]], date: Expression[Option[Calendar]], content: Expression[String]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      mailbox <- mailbox(session)
      flags <- flags(session)
      date <- date(session)
      content <- content(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.Append(id.toString, mailbox, flags, date, content), handler)
    }
  }
}