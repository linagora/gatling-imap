package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.protocol.Response.Disconnected
import com.linagora.gatling.imap.protocol.{ImapResponses, Response}
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.TimeHelper._
import io.gatling.commons.validation.Failure
import io.gatling.core.akka.BaseActor
import io.gatling.core.check.Check
import io.gatling.core.session.Session
import io.gatling.core.stats.message.ResponseTimings

import scala.collection.immutable.Seq

object ImapResponseHandler {
  def props(imapActionContext: ImapActionContext, requestName: String, session: Session, start: Long, checks: Seq[Check[ImapResponses]]): Props = {
    Props(new ImapResponseHandler(imapActionContext, requestName, session, start, checks))
  }
}

class ImapResponseHandler(imapActionContext: ImapActionContext, requestName: String, session: Session, start: Long, checks: Seq[Check[ImapResponses]]) extends BaseActor {
  private val statsEngine = imapActionContext.statsEngine
  private val next = imapActionContext.next

  override def receive: Receive = {
    case Response(responses) =>
      val (checkSaveUpdate, error) = Check.check(responses, session, checks.toList)
      error.fold(ok(session, start, checkSaveUpdate))(ko(session, start))
    case e: Exception =>
      ko(session, start)(Failure(e.getMessage))
    case Disconnected(e) =>
      ko(session, start)(Failure(e.getMessage))
    case msg =>
      logger.error(s"received unexpected message $msg")
  }

  def ko(session: Session, start: Long)(failure: Failure) = {
    statsEngine.logResponse(session, requestName, ResponseTimings(start, nowMillis), KO, None, Some(failure.message))
    next ! session.markAsFailed
    context.stop(self)
  }

  def ok(session: Session, start: Long, updateSession: Session => Session) = {
    statsEngine.logResponse(session, requestName, ResponseTimings(start, nowMillis), OK, None, None)
    next ! updateSession(session)
    context.stop(self)
  }
}