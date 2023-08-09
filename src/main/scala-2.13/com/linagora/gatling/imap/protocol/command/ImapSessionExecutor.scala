package com.linagora.gatling.imap.protocol.command

import akka.actor.ActorRef
import com.linagora.gatling.imap.protocol.{ImapResponses, Response, UserId}
import com.typesafe.scalalogging.Logger
import com.yahoo.imapnio.async.client.ImapFuture
import com.yahoo.imapnio.async.response.ImapAsyncResponse

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

private[command] object ImapSessionExecutor {
  def listen(self: ActorRef, userId: UserId, getResponse: ImapResponses => Response)(logger: Logger)(response: ImapFuture[ImapAsyncResponse]): Unit = {
    listenWithHandler(self, userId, getResponse, _ => ())(logger)(response)
  }

  def listenWithHandler[T](self: ActorRef, userId: UserId, getResponse: ImapResponses => Response, callback: Future[ImapAsyncResponse] => T)(logger: Logger)(response: ImapFuture[ImapAsyncResponse]): T = {
    import collection.JavaConverters._

    callback(Future {
      val responses = response.get()
      val responsesList = ImapResponses(responses.getResponseLines.asScala.to[Seq])
      logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")
      self ! getResponse(responsesList)
      responses
    })
  }
}
