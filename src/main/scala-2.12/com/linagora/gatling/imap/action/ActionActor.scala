/*
 * Copyright 2011-2019 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linagora.gatling.imap.action

import akka.actor.ActorRef
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Validation
import io.gatling.core.action.{Action, ActorDelegatingAction, ExitableAction}
import io.gatling.core.akka.BaseActor
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine

abstract class ActionActor extends BaseActor {

  def next: Action

  override def receive: Receive = {
    case session: Session => execute(session)
  }

  def execute(session: Session): Unit

  /**
   * Makes sure that in case of an actor crash, the Session is not lost but passed to the next Action.
   */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit =
    message.foreach {
      case session: Session =>
        logger.error(s"'${self.path.name}' crashed on session $session, forwarding to the next one", reason)
        next ! session.markAsFailed
      case _ =>
        logger.error(s"'${self.path.name}' crashed on unknown message $message, dropping", reason)
    }
}

abstract class ValidatedActionActor extends ActionActor {

  override def execute(session: Session): Unit =
    executeOrFail(session).onFailure { message =>
      logger.error(s"'${self.path.name}' failed to execute: $message")
      next ! session.markAsFailed
    }

  protected def executeOrFail(session: Session): Validation[_]
}

class ExitableActorDelegatingAction(name: String, val statsEngine: StatsEngine, val clock: Clock, val next: Action, actor: ActorRef) extends ActorDelegatingAction(name, actor) with ExitableAction
