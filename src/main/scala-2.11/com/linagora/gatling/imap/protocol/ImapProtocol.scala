package com.linagora.gatling.imap.protocol

import java.util.Properties

import akka.actor.{ActorRef, ActorSystem}
import com.linagora.gatling.imap.protocol.Command.Disconnect
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolComponents, ProtocolKey}
import io.gatling.core.session.Session

object ImapProtocol {
  val ImapProtocolKey = new ProtocolKey {
    override type Protocol = ImapProtocol
    override type Components = ImapComponents

    override def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[ImapProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def defaultValue(configuration: GatlingConfiguration): ImapProtocol = throw new IllegalStateException("Can't provide a default value for ImapProtocol")

    override def newComponents(system: ActorSystem, coreComponents: CoreComponents): ImapProtocol => ImapComponents = { protocol =>
      val sessions: ActorRef = system.actorOf(ImapSessions.props(protocol), "imapsessions")
      ImapComponents(protocol, sessions)
    }
  }
}

case class ImapComponents(protocol: ImapProtocol, sessions: ActorRef) extends ProtocolComponents {
  override def onStart: Option[(Session) => Session] = None

  override def onExit: Option[(Session) => Unit] = Some(session => sessions ! Disconnect(session.userId.toString))
}

case class ImapProtocol(host: String,
                        port: Int = 143,
                        config: Properties = new Properties()
                       ) extends Protocol
