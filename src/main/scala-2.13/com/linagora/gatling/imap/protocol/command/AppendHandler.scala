package com.linagora.gatling.imap.protocol.command

import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import java.util.regex.Pattern

import akka.actor.{ActorRef, Props}
import com.linagora.gatling.imap.action.BaseActor
import com.linagora.gatling.imap.protocol._
import com.linagora.gatling.imap.protocol.command.AppendHandler.crLfRegex
import com.yahoo.imapnio.async.client.ImapAsyncSession
import com.yahoo.imapnio.async.request.AppendCommand
import com.yahoo.imapnio.async.response.ImapAsyncResponse
import javax.mail.Flags

import scala.collection.immutable.Seq

object AppendHandler {
  def props(session: ImapAsyncSession) = Props(new AppendHandler(session))

  private val crLfRegex = Pattern.compile("(?<!\r)\n")
}

class AppendHandler(session: ImapAsyncSession) extends BaseActor {

  override def receive: Receive = {
    case Command.Append(userId, mailbox, flags, date, content) =>
      if (date.isDefined) throw new NotImplementedError("Date parameter for APPEND is still not implemented")

      logger.debug(s"APPEND receive from sender ${sender.path} on ${self.path}")
      context.become(waitCallback(sender()))
      val nullDate = null
      val crLfContent = crLfRegex.matcher(content).replaceAll("\r\n").getBytes(StandardCharsets.UTF_8)

      val responseCallback: Consumer[ImapAsyncResponse] = responses => {
        import scala.jdk.CollectionConverters._

        val responsesList = ImapResponses(responses.getResponseLines.asScala.toSeq)
        logger.trace(s"On response for $userId :\n ${responsesList.mkString("\n")}")

        self ! Response.Appended(responsesList)
      }

      val errorCallback: Consumer[Exception] = e => {
        logger.trace(s"${getClass.getSimpleName} command failed", e)
        logger.error(s"${getClass.getSimpleName} command failed")
        sender ! e
        context.stop(self)
      }

      val future = session.execute(new AppendCommand(mailbox, flags.map(toImapFlags).orNull, nullDate, crLfContent))
      future.setDoneCallback(responseCallback)
      future.setExceptionCallback(errorCallback)
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

