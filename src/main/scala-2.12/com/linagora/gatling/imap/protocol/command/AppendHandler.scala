package com.linagora.gatling.imap.protocol.command

import java.nio.charset.StandardCharsets

import javax.mail.Flags

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.protocol._
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.AppendCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import io.gatling.core.akka.BaseActor

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object AppendHandler {
  def props(session: ImapAsyncSession) = Props(new AppendHandler(session))
}

class AppendHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.Append(userId, mailbox, flags, date, content) =>
      if (date.isDefined) throw new NotImplementedError("Date parameter for APPEND is still not implemented")

      logger.debug(s"APPEND receive from sender ${sender.path} on ${self.path}")
      context.become(waitCallback(sender()))
      val nullDate = null
      val crLfContent = content.replaceAll("(?<!\r)\n", "\r\n").getBytes(StandardCharsets.UTF_8)
      ImapSessionExecutor
        .listenWithHandler(self, userId, Response.Appended, callback)(logger)(session.execute(new AppendCommand(mailbox, flags.map(toImapFlags).orNull, nullDate, crLfContent)))
  }

  private def callback(response: Future[ImapAsyncResponse]) = {
    Try(response) match {
      case Success(futureResult) =>
        futureResult.onComplete(future => {
          logger.debug(s"AppendHandler command completed, success : ${future.isSuccess}")
          if (!future.isSuccess) {
            logger.error("AppendHandler command failed", future.toEither.left)
          }

        })
      case Failure(e) =>
        logger.error("ERROR when executing APPEND COMMAND", e)
        throw e
    }
  }

  private def toImapFlags(flags: Seq[String]): Flags = {
    val imapFlags = new Flags()
    flags.foreach(imapFlags.add)
    imapFlags
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Appended(_) =>
      logger.debug(s"APPEND reply to sender ${sender.path}")
      sender ! msg
      context.stop(self)
  }

}

