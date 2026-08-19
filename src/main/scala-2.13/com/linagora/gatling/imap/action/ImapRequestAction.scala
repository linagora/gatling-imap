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