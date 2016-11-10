package com.linagora.gatling.imap.action

import akka.actor.ActorRef
import io.gatling.core.action.Action
import io.gatling.core.stats.StatsEngine

case class ImapActionContext(sessions: ActorRef, statsEngine: StatsEngine, next: Action)
