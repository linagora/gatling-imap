package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.Command
import com.linagora.gatling.imap.protocol.command.{FetchAttributes, FetchRange}
import io.gatling.commons.util.TimeHelper._
import io.gatling.commons.validation.Validation
import io.gatling.core.action.ValidatedActionActor
import io.gatling.core.session._

import scala.collection.immutable.Seq

object FetchAction {
  def props(imapContext: ImapActionContext, requestname: String, checks: Seq[ImapCheck], sequence: Expression[Seq[FetchRange]], attributes: Expression[FetchAttributes]) =
    Props(new FetchAction(imapContext, requestname, checks, sequence, attributes))
}

class FetchAction(val imapContext: ImapActionContext, val requestName: String, override val checks: Seq[ImapCheck], sequence: Expression[Seq[FetchRange]], attributes: Expression[FetchAttributes]) extends ValidatedActionActor with ImapActionActor {

  override protected def executeOrFail(session: Session): Validation[_] = {
    for {
      sequence <- sequence(session)
      attributes <- attributes(session)
    } yield {
      val id: Long = session.userId
      val handler = handleResponse(session, nowMillis)
      sessions.tell(Command.Fetch(id.toString, sequence, attributes), handler)
    }
  }
}