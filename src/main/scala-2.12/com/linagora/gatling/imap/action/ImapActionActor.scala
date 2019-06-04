package com.linagora.gatling.imap.action

import akka.actor.{Actor, ActorRef}
import com.linagora.gatling.imap.protocol.ImapResponses
import io.gatling.core.action.Action
import io.gatling.core.check.Check
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine

import scala.collection.immutable.Seq

trait ImapActionActor {
  self: Actor =>

  def imapContext: ImapActionContext

  def requestName: String

  def statsEngine: StatsEngine = imapContext.statsEngine

  def next: Action = imapContext.next

  def sessions: ActorRef = imapContext.sessions

  def checks: Seq[Check[ImapResponses]] = Seq.empty

  def successOnDisconnect : Boolean = false

  protected def handleResponse(session: Session, start: Long): ActorRef =
    context.actorOf(ImapResponseHandler.props(imapContext, requestName, session, start, checks, successOnDisconnect))
}
