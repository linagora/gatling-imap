package com.linagora.gatling.imap.protocol.command

abstract class FetchAttributes {
  def asString: String
}

object FetchAttributes {

  case class ALL() extends FetchAttributes {
    override def asString: String = "ALL"
  }

  case class FULL() extends FetchAttributes {
    override def asString: String = "FULL"
  }

  case class FAST() extends FetchAttributes {
    override def asString: String = "FAST"
  }

  case class AttributeList(fetchAttributes: String*) extends FetchAttributes {
    override def asString: String = fetchAttributes.mkString(" ")
  }

}