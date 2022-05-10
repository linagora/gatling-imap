package com.linagora.gatling.imap

import com.linagora.gatling.imap.protocol.{Domain, User}
import io.gatling.core.feeder.FeederBuilder

object Fixture {
  val simpson = Domain("simpson.cartoon")
  val bart = User("bart@simpson.cartoon", "Eat My Shorts")
  val cyrusAdmin = User("cyrus", "cyrus")
  val homer = User("homer@simpson.cartoon", "Mmm... donuts")

  def feederBuilder(users: User*): FeederBuilder = () => {
    val feeder = users.map(user => Map("username" -> user.login, "password" -> user.password))
    feeder.toIterator
  }
}
