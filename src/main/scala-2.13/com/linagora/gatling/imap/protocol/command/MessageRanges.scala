package com.linagora.gatling.imap.protocol.command

import com.yahoo.imapnio.async.data.MessageNumberSet

case class MessageRanges(messageRanges: MessageRange*) {
  def asImap: Array[MessageNumberSet] = messageRanges.map(_.asImap).toArray
}
