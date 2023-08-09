package com.linagora.gatling.imap.action

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

import akka.actor.{ Actor, ActorSystem, Scheduler, Terminated }
import com.typesafe.scalalogging.LazyLogging

/**
 * Cloned from io.gatling.core.akka.BaseActor
 */
abstract class BaseActor extends Actor with LazyLogging {
  implicit def system: ActorSystem = context.system
  def scheduler: Scheduler = system.scheduler
  implicit def dispatcher: ExecutionContext = system.dispatcher

  // FIXME is ReceiveTimeout set up by default?
  override def preStart(): Unit = context.setReceiveTimeout(Duration.Undefined)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit =
    logger.error(s"Actor $this crashed on message $message", reason)

  override def unhandled(message: Any): Unit =
    message match {
      case _: Terminated => super.unhandled(message)
      case unknown       => throw new IllegalArgumentException(s"Actor $this doesn't support message $unknown")
    }
}
