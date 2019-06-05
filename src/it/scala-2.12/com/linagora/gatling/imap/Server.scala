package com.linagora.gatling.imap

import com.linagora.gatling.imap.protocol.{Domain, User}

trait Server {
  def start(): RunningServer
}

trait RunningServer {
  def addUser(user: User): Unit

  def addDomain(domain: Domain): Unit

  def stop(): Unit

  def mappedImapPort(): Integer
}
