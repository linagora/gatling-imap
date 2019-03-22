package com.linagora.gatling.imap.action

import akka.actor.ActorRef
import io.gatling.commons.util.Clock
import io.gatling.core.action.Action
import io.gatling.core.stats.StatsEngine

case class ImapActionContext(clock: Clock, sessions: ActorRef, statsEngine: StatsEngine, next: Action)
