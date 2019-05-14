package com.linagora.gatling.imap.protocol

import scala.util.Properties

object ImapProtocolBuilder {
  val HOSTNAME = Properties.envOrElse("TARGET_HOSTNAME", "localhost")
  val PORT = Properties.envOrElse("IMAP_PORT", "143").toInt
  val default = new ImapProtocolBuilder(HOSTNAME, PORT)
}

case class ImapProtocolBuilder(host: String, port: Int) {

  def host(host: String): ImapProtocolBuilder = copy(host = host)

  def port(port: Int): ImapProtocolBuilder = copy(port = port)

  def build(): ImapProtocol = new ImapProtocol(host, port)

}
