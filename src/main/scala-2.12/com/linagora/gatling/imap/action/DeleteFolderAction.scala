package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.{Command, UserId}
import io.gatling.commons.validation.Validation
import io.gatling.core.session._

import scala.collection.immutable.Seq

object DeleteFolderAction {
  def props(imapContext: ImapActionContext, requestName: String, checks: Seq[ImapCheck], mailbox: Expression[String]) =
    Props(new DeleteFolderAction(imapContext, requestName, checks, mailbox))
}

class DeleteFolderAction(val imapContext: ImapActionContext,
                         val requestName: String,
                         override val checks: Seq[ImapCheck],
                         mailbox: Expression[String]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      mailbox <- mailbox(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, imapContext.clock.nowMillis)
      sessions.tell(Command.DeleteFolder(UserId(id), mailbox), handler)
    }
  }
}
