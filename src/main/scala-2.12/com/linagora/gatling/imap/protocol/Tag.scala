package com.linagora.gatling.imap.protocol

object Tag {
  val initial = Tag(1)
}

case class Tag(value: Int) extends AnyVal {
  def next = copy(value = value + 1)

  def string: String = s"A$value"
}
