package com.linagora.gatling.imap.action

import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.regex.Pattern

import com.linagora.gatling.imap.check.ImapCheck
import com.yahoo.imapnio.async.request.AppendCommand
import io.gatling.commons.validation.Validation
import io.gatling.core.session._
import javax.mail.Flags

import scala.collection.immutable.Seq

class AppendAction(imapContext: ImapActionContext,
                   requestName: String,
                   checks: Seq[ImapCheck],
                   mailbox: Expression[String],
                   flags: Expression[Option[Seq[String]]],
                   date: Expression[Option[Calendar]],
                   content: Expression[String]) extends ImapRequestAction(imapContext, requestName, checks) {

  private val crLfRegex = Pattern.compile("(?<!\r)\n")

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      mailbox <- mailbox(session)
      flags <- flags(session)
      date <- date(session)
      content <- content(session)
      s <- sessionFor(session)
    } yield {
      if (date.isDefined) throw new NotImplementedError("Date parameter for APPEND is still not implemented")
      val crLfContent = crLfRegex.matcher(content).replaceAll("\r\n").getBytes(StandardCharsets.UTF_8)
      val future = s.execute(new AppendCommand(mailbox, flags.map(toImapFlags).orNull, null, crLfContent))
      future.setDoneCallback(responses => handleResponse(session, start)(toImapResponses(responses)))
      future.setExceptionCallback(e => handleError(session, start)(e))
    }
  }

  private def toImapFlags(flags: Seq[String]): Flags = {
    val imapFlags = new Flags()
    flags.foreach(imapFlags.add)
    imapFlags
  }
}