package com.linagora.gatling.imap

import com.linagora.gatling.imap.protocol.User

trait Server {
  def start(): RunningServer
}

trait RunningServer {
  def addUser(user: User): Unit = addUser(user.login, user.password)
  def addUser(login: String, password: String): Unit
  def stop(): Unit
  def mappedImapPort(): Integer
}
