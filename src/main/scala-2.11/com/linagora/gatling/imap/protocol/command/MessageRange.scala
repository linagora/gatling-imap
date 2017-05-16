package com.linagora.gatling.imap.protocol.command

/**
  * Created by lduzan on 16/05/17.
  */
object MessageRange {
  case class From(from: Long) extends MessageRange {
    override def asString = s"$from:*"
  }

  case class To(to: Long) extends MessageRange {
    override def asString = s"*:$to"
  }

  case class One(value: Long) extends MessageRange {
    override def asString = value.toString
  }

  case class Range(from: Long, to: Long) extends MessageRange {
    override def asString = s"$from:$to"
  }

  case class Last() extends MessageRange {
    override def asString = "*:*"
  }
}

abstract class MessageRange {
  def asString: String
}