package com.linagora.gatling.imap.action

import com.linagora.gatling.imap.protocol.ImapComponents
import io.gatling.commons.util.Clock
import io.gatling.core.action.Action
import io.gatling.core.stats.StatsEngine

case class ImapActionContext(clock: Clock, components: ImapComponents, statsEngine: StatsEngine, next: Action)