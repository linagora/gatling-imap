package com.linagora.gatling.imap.protocol.command

case class MessageRanges(val messageRange: MessageRange*) {
  def asString: String = messageRange.map(_.asString).mkString(",")
}
