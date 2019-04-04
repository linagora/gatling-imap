package com.linagora.gatling.imap.protocol

import java.net.URI
import java.util
import java.util.Properties

import akka.actor.{ActorRef, Props, Stash}
import com.lafaspot.imapnio.client.{IMAPClient, IMAPSession => ClientSession}
import com.lafaspot.imapnio.listener.IMAPConnectionListener
import com.lafaspot.logfast.logging.internal.LogPage
import com.lafaspot.logfast.logging.{LogManager, Logger}
import com.linagora.gatling.imap.protocol.command._
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor
import io.gatling.core.util.NameGen

import scala.util.control.NoStackTrace

case class UserId(value: Long) extends AnyVal

object ImapSessions {
  def props(protocol: ImapProtocol): Props = Props(new ImapSessions(protocol))
}

class ImapSessions(protocol: ImapProtocol) extends BaseActor {
  val imapClient = new IMAPClient(4)

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
  def props(client: IMAPClient, protocol: ImapProtocol): Props =
    Props(new ImapSession(client, protocol))

}

private class ImapSession(client: IMAPClient, protocol: ImapProtocol) extends BaseActor with Stash with NameGen {
  val connectionListener = new IMAPConnectionListener {
    override def onConnect(session: ClientSession): Unit = {
      logger.trace("Callback onConnect called")
      self ! Response.Connected(ImapResponses.empty)
    }

    override def onMessage(session: ClientSession, response: IMAPResponse): Unit =
      logger.trace("Callback onMessage called")


    override def onDisconnect(session: ClientSession, cause: Throwable): Unit = {
      logger.trace("Callback onDisconnect called")
      self ! Response.Disconnected(cause)
    }

    override def onInactivityTimeout(session: ClientSession): Unit =
      logger.trace("Callback onInactivityTimeout called")

    override def onResponse(session: ClientSession, tag: String, responses: util.List[IMAPResponse]): Unit =
      logger.trace("Callback onResponse called")
  }
  val uri = new URI(s"imap://${protocol.host}:${protocol.port}")
  val config: Properties = protocol.config
  logger.debug(s"connecting to $uri with $config")
  val session: ClientSession = client.createSession(uri, config, connectionListener, new LogManager(Logger.Level.ERROR, LogPage.DEFAULT_SIZE))
  private var currentTag: Tag = Tag.initial

  override def receive: Receive = disconnected

  def disconnected: Receive = {
    case Command.Connect(userId) =>
      logger.debug(s"got connect request, $userId connecting to $uri")
      session.connect()
      context.become(connecting(sender()))
    case Response.Disconnected(_) => ()
    case Command.Disconnect(_) => ()
    case msg =>
      logger.error(s"disconnected - unexpected message from ${sender.path} " + msg)
      if (sender.path != self.path)
        sender ! ImapStateError(s"session for ${self.path.name} is not connected")
  }

  def connecting(receiver: ActorRef): Receive = {
    case msg@Response.Connected(responses) =>
      logger.debug("got connected response")
      context.become(connected)
      receiver ! msg
      unstashAll()
    case msg@Response.Disconnected(cause) =>
      context.become(disconnected)
      receiver ! msg
      unstashAll()
    case x =>
      logger.error(s"connecting - got unexpected message $x")
      stash
  }

  def connected: Receive = {
    case cmd@Command.Login(_, _, _) =>
      val handler = context.actorOf(LoginHandler.props(session, nextTag()), genName("login"))
      handler forward cmd
    case cmd@Command.Select(_, _) =>
      val handler = context.actorOf(SelectHandler.props(session, nextTag()), genName("select"))
      handler forward cmd
    case cmd@Command.List(_, _, _) =>
      val handler = context.actorOf(ListHandler.props(session, nextTag()), genName("list"))
      handler forward cmd
    case cmd@Command.Fetch(_, _, _) =>
      val handler = context.actorOf(FetchHandler.props(session, nextTag()), genName("fetch"))
      handler forward cmd
    case cmd@Command.UIDFetch(_, _, _) =>
      val handler = context.actorOf(UIDFetchHandler.props(session, nextTag()), genName("uidFetch"))
      handler forward cmd
    case cmd@Command.Store(_, _, _) =>
      val handler = context.actorOf(StoreHandler.props(session, nextTag()), genName("store"))
      handler forward cmd
    case cmd@Command.Expunge(_) =>
      val handler = context.actorOf(ExpungeHandler.props(session, nextTag()), genName("expunge"))
      handler forward cmd
    case cmd@Command.Append(_, _, _, _, _) =>
      val handler = context.actorOf(AppendHandler.props(session, nextTag()), genName("append"))
      handler forward cmd
    case msg@Response.Disconnected(cause) =>
      context.become(disconnected)
    case msg@Command.Disconnect(userId) =>
      session.disconnect()
      context.become(disconnecting(sender()))

  }

  private def nextTag() = {
    val tag = currentTag
    currentTag = tag.next
    tag
  }

  def disconnecting(receiver: ActorRef): Receive = {
    case msg@Response.Disconnected(cause) =>
      receiver ! msg
      context.become(disconnected)
      unstashAll()
    case x =>
      logger.error(s"disconnecting - got unexpected message $x")
      stash
  }
}

case class ImapStateError(msg: String) extends IllegalStateException(msg) with NoStackTrace

