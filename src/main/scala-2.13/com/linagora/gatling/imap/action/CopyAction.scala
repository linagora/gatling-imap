package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.command.MessageRanges
import com.linagora.gatling.imap.protocol.{Command, UserId}
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

object CopyAction {
  def props(imapContext: ImapActionContext, requestName: String, checks: Seq[ImapCheck], sequence: Expression[MessageRanges], mailbox: Expression[String]) =
    Props(new CopyAction(imapContext, requestName, checks, sequence, mailbox))
}

class CopyAction(val imapContext: ImapActionContext,
                 val requestName: String,
                 override val checks: Seq[ImapCheck],
                 sequence: Expression[MessageRanges],
                 mailbox: Expression[String]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      sequence <- sequence(session)
      mailbox <- mailbox(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.Copy(UserId(id), sequence, mailbox), handler)
    }
  }
}