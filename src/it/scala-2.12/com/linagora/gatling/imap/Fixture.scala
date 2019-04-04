package com.linagora.gatling.imap

import com.linagora.gatling.imap.protocol.User
import io.gatling.core.feeder.FeederBuilder

object Fixture {
  val bart = User("bart@simpson.cartoon", "Eat My Shorts")
  val homer = User("homer@simpson.cartoon", "Mmm... donuts")

  def feederBuilder(users: User*): FeederBuilder = () => {
    val feeder = users.map(user => Map("username" -> user.login, "password" -> user.password))
    println(s"Feeder : $feeder")
    feeder.toIterator
  }
}
