package com.linagora.gatling.imap

import com.linagora.gatling.imap.action.ImapActionBuilder
import com.linagora.gatling.imap.check.ImapCheckSupport
import com.linagora.gatling.imap.protocol.{ImapProtocol, ImapProtocolBuilder}

import scala.language.implicitConversions

object PreDef extends ImapCheckSupport {

  def imap: ImapProtocolBuilder = ImapProtocolBuilder.default

  implicit def imapProtocolBuilder2ImapProtocol(builder: ImapProtocolBuilder): ImapProtocol = builder.build()

  def imap(requestName: String): ImapActionBuilder = new ImapActionBuilder(requestName)
}
