package com.linagora.gatling.imap.protocol

import java.net.URI
import java.util.Properties
import java.util.concurrent.ConcurrentLinkedQueue

import akka.actor.{Props, Stash}
import com.linagora.gatling.imap.protocol.command._
import com.yahoo.imapnio.async.client.ImapAsyncSession.DebugMode
import com.yahoo.imapnio.async.client.{ImapAsyncClient, ImapAsyncSession, ImapAsyncSessionConfig}
import io.gatling.core.akka.BaseActor
import io.gatling.core.util.NameGen

import scala.util.control.NoStackTrace

case class UserId(value: Long) extends AnyVal

object ImapSessions {
  def props(protocol: ImapProtocol): Props = Props(new ImapSessions(protocol))
}

class ImapSessions(protocol: ImapProtocol) extends BaseActor {
  private val imapClient = {
    val numOfThreads = 8
    new ImapAsyncClient(numOfThreads)
  }

  override def receive: Receive = {
    case cmd: Command =>
      sessionFor(cmd.userId).forward(cmd)
  }

  private def sessionFor(userId: UserId) = {
    context.child(userId.value.toString).getOrElse(createImapSession(userId))
  }

  protected def createImapSession(userId: UserId) = {
    context.actorOf(ImapSession.props(imapClient, protocol), userId.value.toString)
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    super.postStop()
    imapClient.shutdown()
  }
}

private object ImapSession {
  def props(client: => ImapAsyncClient, protocol: ImapProtocol): Props =
    Props(new ImapSession(client, protocol))

}

private class ImapSession(client: => ImapAsyncClient, protocol: ImapProtocol) extends BaseActor with Stash with NameGen {
  val uri = new URI(s"${protocol.protocol}://${protocol.host}:${protocol.port}")
  val config: Properties = protocol.config
  logger.debug(s"connecting to $uri with $config")
  val session: ImapAsyncSession = {
    val config = new ImapAsyncSessionConfig
    config.setConnectionTimeoutMillis(50000)
    config.setReadTimeoutMillis(60000)
    val sniNames = null

    val localAddress = null
    client
      .createSession(uri, config, localAddress, sniNames, DebugMode.DEBUG_OFF, "ImapSession")
      .get()
      .getSession
  }

  override def receive: Receive = disconnected

  def disconnected: Receive = {
    case Command.Connect(userId) =>
      logger.debug(s"got connect request, $userId connecting to $uri")
      context.become(connected)
      sender() ! Response.Connected(ImapResponses.empty)
    case Response.Disconnected(_) => ()
    case Command.Disconnect(_) => ()
    case msg =>
      logger.error(s"disconnected - unexpected message from ${sender.path} ${msg}")
      if (sender.path != self.path)
        sender ! ImapStateError(s"session for ${self.path.name} is not connected")
  }

  def connected: Receive = {
    case cmd@Command.Login(_, _, _) =>
      val handler = context.actorOf(LoginHandler.props(session), genName("login"))
      handler forward cmd
    case cmd@Command.Select(_, _) =>
      val handler = context.actorOf(SelectHandler.props(session), genName("select"))
      handler forward cmd
    case cmd@Command.List(_, _, _) =>
      val handler = context.actorOf(ListHandler.props(session), genName("list"))
      handler forward cmd
    case cmd@Command.Fetch(_, _, _) =>
      val handler = context.actorOf(FetchHandler.props(session), genName("fetch"))
      handler forward cmd
    case cmd@Command.UIDFetch(_, _, _) =>
      val handler = context.actorOf(UIDFetchHandler.props(session), genName("uidFetch"))
      handler forward cmd
    case cmd@Command.Store(_, _, _) =>
      val handler = context.actorOf(StoreHandler.props(session), genName("store"))
      handler forward cmd
    case cmd@Command.Expunge(_) =>
      val handler = context.actorOf(ExpungeHandler.props(session), genName("expunge"))
      handler forward cmd
    case cmd@Command.Append(_, _, _, _, _) =>
      val handler = context.actorOf(AppendHandler.props(session), genName("append"))
      handler forward cmd
    case Command.Disconnect(userId) =>
      session.close().get()
      context.become(disconnected)
      sender() ! Response.Disconnected(new RuntimeException(s"Disconnected command for $userId"))
    case message =>
      logger.error(s"connected - got unexpected message $message")

  }
}

case class ImapStateError(msg: String) extends IllegalStateException(msg) with NoStackTrace

