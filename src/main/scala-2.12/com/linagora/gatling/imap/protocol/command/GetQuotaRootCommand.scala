package com.linagora.gatling.imap.protocol.command

import java.nio.charset.StandardCharsets

import com.yahoo.imapnio.async.request.{ImapCommandType, ImapRequestAdapter}
import io.netty.buffer.{ByteBuf, Unpooled}

case object GetQuotaRootCommandType extends ImapCommandType {
  override def getType: String = "GETQUOTAROOT"
}

class GetQuotaRootCommand(mailbox: String) extends ImapRequestAdapter {
  override def getCommandLineBytes: ByteBuf = {
    val sb = Unpooled.buffer(GetQuotaRootCommandType.getType.getBytes(StandardCharsets.US_ASCII).length + mailbox.getBytes(StandardCharsets.US_ASCII).length + 100)
    sb.writeBytes(GetQuotaRootCommandType.getType.getBytes(StandardCharsets.US_ASCII))
    sb.writeBytes(" ".getBytes(StandardCharsets.US_ASCII))
    sb.writeBytes(mailbox.getBytes(StandardCharsets.US_ASCII))
    sb.writeBytes("\r\n".getBytes(StandardCharsets.US_ASCII))
    sb
  }

  override def getCommandType: ImapCommandType = GetQuotaRootCommandType

  override def cleanup(): Unit = {

  }
}
