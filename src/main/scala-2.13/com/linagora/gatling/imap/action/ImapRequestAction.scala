package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.{ImapComponents, ImapResponses, UserId}
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.{Failure, Success, Validation}
import io.gatling.core.action.{Action, RequestAction}
import io.gatling.core.check.Check
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.response.ImapAsyncResponse

import scala.collection.immutable.Seq

abstract class ImapRequestAction(val imapContext: ImapActionContext, val name: String, val checks: Seq[ImapCheck]) extends RequestAction {

  override def clock: Clock = imapContext.clock

  override def statsEngine: StatsEngine = imapContext.statsEngine

  override def next: Action = imapContext.next

  override def requestName: Expression[String] = _ => Success(name)

  protected def components: ImapComponents = imapContext.components

  protected def sessionFor(session: Session): Validation[ImapAsyncSession] = {
    val s = components.sessionFor(UserId(session.userId))
    if (s == null) Failure(s"IMAP session not connected for user ${session.userId}")
    else Success(s)
  }

  // ponytail: ImapAsyncSession.execute() throws ImapAsyncClientException synchronously when the
  // channel is already closed (e.g. server dropped the connection between commands). The old actor
  // code had a disconnected/connected state machine that prevented reaching execute(); the migration
  // dropped it, so the throw propagated as a Gatling "crash" (logged twice) and cascaded to every
  // remaining action. Catch it here and treat it as a normal KO. Failure (no session) is also a KO
  // instead of logRequestCrash so it stays off the error log. See issue #89.
  override def execute(session: Session): Unit = {
    val start = clock.nowMillis
    try {
      sendRequest(session) match {
        case Failure(error) => ko(session, start, error)
        case _ =>
      }
    } catch {
      case e: Exception => handleError(session, start)(e)
    }
  }

  protected def toImapResponses(responses: ImapAsyncResponse): ImapResponses = {
    import scala.jdk.CollectionConverters._
    ImapResponses(responses.getResponseLines.asScala.toSeq)
  }

  protected def handleResponse(session: Session, start: Long)(responses: ImapResponses): Unit = {
    val (newSession, error) = Check.check(responses, session, checks.toList)
    error match {
      case Some(failure) => ko(session, start, failure.message)
      case None => ok(newSession, start)
    }
  }

  protected def handleError(session: Session, start: Long)(e: Exception): Unit = {
    logger.error(s"$name command failed", e)
    // ponytail: any exception reaching here means the channel is dead/closing (channel exception,
    // timeout, disconnect, or execute() on an already-closed channel). Drop the session from the
    // map so the following actions fail with a clean KO ("session not connected") instead of
    // repeating OPERATION_PROHIBITED_ON_CLOSED_CHANNEL for every remaining request. See issue #89.
    components.disconnect(UserId(session.userId))
    ko(session, start, e.getMessage)
  }

  private def ok(session: Session, start: Long): Unit = {
    statsEngine.logResponse(session.scenario, session.groups, name, start, clock.nowMillis, OK, None, None)
    next ! session
  }

  private def ko(session: Session, start: Long, message: String): Unit = {
    statsEngine.logResponse(session.scenario, session.groups, name, start, clock.nowMillis, KO, None, Some(message))
    next ! session.markAsFailed
  }
}