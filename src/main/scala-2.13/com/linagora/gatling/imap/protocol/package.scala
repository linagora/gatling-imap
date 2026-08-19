package com.linagora.gatling.imap

package object protocol {

  case class Domain(value: String) extends AnyVal
  case class User(login: String, password: String)

  case class UserId(value: Long) extends AnyVal

  case class StatusItems(items: Seq[StatusItem])

  // 33 STATUS "test" (UIDNEXT MESSAGES UNSEEN RECENT)
  sealed trait StatusItem {
    def asString: String
  }
  case object UidNext extends StatusItem {
    override def asString: String = "UIDNEXT"
  }
  case object Messages extends StatusItem {
    override def asString: String = "MESSAGES"
  }
  case object Unseen extends StatusItem {
    override def asString: String = "UNSEEN"
  }
  case object Recent extends StatusItem {
    override def asString: String = "RECENT"
  }
}