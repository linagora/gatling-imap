package com.linagora.gatling.imap.protocol.command

import java.util

import akka.actor.ActorRef
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.linagora.gatling.imap.protocol.{ImapResponses, Response, UserId}
import com.sun.mail.imap.protocol.IMAPResponse
import com.typesafe.scalalogging.Logger

import scala.collection.immutable.Seq

private[command] class RespondToActorIMAPCommandListener(self: ActorRef,
                                                         userId: UserId,
                                                         getResponse: ImapResponses => Response,
                                                         doOnMessage: (IMAPSession, IMAPResponse) => Unit = (_, _) => ())(logger: Logger)
  extends IMAPCommandListener {

  import collection.JavaConverters._

  override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
    logsOnMessage(response)
    doOnMessage(session, response)
  }

  protected def logsOnMessage(response: IMAPResponse): Unit = {
    logger.trace(s"Untagged message for $userId : ${response.toString}")
  }

  override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
    val response = ImapResponses(responses.asScala.to[Seq])
    logsOnResponse(response)
    self ! getResponse(response)
  }

  protected def logsOnResponse(response: ImapResponses): Unit = {
    logger.trace(s"On response for $userId :\n ${response.mkString("\n")}")
  }
}
