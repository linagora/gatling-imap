package com.linagora.gatling.imap.protocol


case class Uid(value: Int) {
  require(value > 0)
}
