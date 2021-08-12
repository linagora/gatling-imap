package com.linagora.gatling.imap.protocol

import scala.util.Properties

object ImapProtocolBuilder {
  val HOSTNAME = Properties.envOrElse("TARGET_HOSTNAME", "localhost")
  val PORT = Properties.envOrElse("IMAP_PORT", "143").toInt
  val PROTOCOL = Properties.envOrElse("IMAP_PROTOCOL", "imap")
  val default = new ImapProtocolBuilder(HOSTNAME, PORT, PROTOCOL)
}

case class ImapProtocolBuilder(host: String, port: Int, protocol: String) {

  def host(host: String): ImapProtocolBuilder = copy(host = host)

  def port(port: Int): ImapProtocolBuilder = copy(port = port)

  def protocol(protocol: String): ImapProtocolBuilder = copy(protocol = protocol)

  def build(): ImapProtocol = new ImapProtocol(host, port, protocol)

}
