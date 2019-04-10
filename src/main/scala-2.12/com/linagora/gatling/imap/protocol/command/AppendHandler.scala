package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.channel.IMAPChannelFuture
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPChannelFutureListener
import com.linagora.gatling.imap.protocol._
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor

import scala.util.{Failure, Success, Try}

object AppendHandler {
  def props(session: IMAPSession, tag: Tag) = Props(new AppendHandler(session, tag))
}

class AppendHandler(session: IMAPSession, tag: Tag) extends BaseActor {

  private[this] def executeTextCommand(session: IMAPSession)(textCommand: String): Unit = {
    logger.trace(s"execute APPEND TEXT COMMAND : $textCommand")
    Try(session.executeRawTextCommand(textCommand)) match {
      case Success(value) => //DO NOTHING
      case Failure(e) =>
        logger.error("ERROR when executing APPEND TEXT COMMAND", e)
        throw e
    }
  }

  private[this] def doOnMessageForContent(content: String): (IMAPSession, IMAPResponse) => Unit = (session, response) => {
    if (response.isContinuation) {
      content.lines.foreach(executeTextCommand(session))
    }
  }

  override def receive: Receive = {
    case Command.Append(userId, mailbox, flags, date, content) =>
      if (!date.isEmpty) throw new NotImplementedError("Date parameter for APPEND is still not implemented")

      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Appended, doOnMessage = doOnMessageForContent(content))(logger)
      val flagsAsString = flags.map(_.mkString("(", " ", ")")).getOrElse("()")
      val length = content.length + content.lines.length - 1
      logger.debug(s"APPEND receive from sender ${sender.path} on ${self.path}")
      context.become(waitCallback(sender()))
      Try(session.executeAppendCommand(tag.string, mailbox, flagsAsString, length.toString, listener)) match {
        case Success(futureResult) =>
          futureResult.addListener(new IMAPChannelFutureListener {
            override def operationComplete(future: IMAPChannelFuture): Unit = {
              logger.debug(s"AppendHandler command completed, success : ${future.isSuccess}")
              if (!future.isSuccess) {
                logger.error("AppendHandler command failed", future.cause())
              }

            }
          })
        case Failure(e) =>
          logger.error("ERROR when executing APPEND COMMAND", e)
          throw e
      }
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Appended(response) =>
      logger.debug(s"APPEND reply to sender ${sender.path}")
      sender ! msg
      context.stop(self)
  }

}

