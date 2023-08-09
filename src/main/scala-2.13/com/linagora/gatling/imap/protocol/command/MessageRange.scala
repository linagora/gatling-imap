package com.linagora.gatling.imap.protocol.command

import com.yahoo.imapnio.async.data.MessageNumberSet

object MessageRange {
  case class From(from: Long) extends MessageRange {
    override def asImap = new MessageNumberSet(from, MessageNumberSet.LastMessage.LAST_MESSAGE)
  }

  case class To(to: Long) extends MessageRange {
    override def asImap = new MessageNumberSet(0, to) // FIXME -- should be "*:$to"
  }

  case class One(value: Long) extends MessageRange {
    override def asImap = new MessageNumberSet(value, value)
  }

  case class Range(from: Long, to: Long) extends MessageRange {
    override def asImap = new MessageNumberSet(from, to)
  }

  case class Last() extends MessageRange {
    override def asImap = new MessageNumberSet(MessageNumberSet.LastMessage.LAST_MESSAGE)
  }
}

sealed abstract class MessageRange {
  def asImap: MessageNumberSet
}