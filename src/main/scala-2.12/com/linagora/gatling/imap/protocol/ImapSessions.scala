package com.linagora.gatling.imap.protocol

import java.net.URI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Properties
import java.util.function.Consumer

import akka.actor.{Props, Stash}
import com.linagora.gatling.imap.protocol.command._
import com.yahoo.imapnio.async.client.ImapAsyncSession.DebugMode
import com.yahoo.imapnio.async.client.{ImapAsyncClient, ImapAsyncSession, ImapAsyncSessionConfig, ImapFuture}
import com.yahoo.imapnio.async.internal.ImapAsyncSessionImpl
import io.gatling.core.akka.BaseActor
import io.gatling.core.util.NameGen
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import scala.util.Try
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

  private val TRUST_ALL_CERTS: Array[TrustManager] = Array[TrustManager](new X509TrustManager() {
    override def getAcceptedIssuers: Array[X509Certificate] = return new Array[X509Certificate](0)

    override def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = {
    }

    override def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = {
    }
  })
  val sslContext = {
    // Create a trust manager that does not validate certificate chains
    val sc: SSLContext = SSLContext.getInstance("SSL")
    sc.init(null, TRUST_ALL_CERTS, new SecureRandom)
    sc
  }
}

private class ImapSession(client: => ImapAsyncClient, protocol: ImapProtocol) extends BaseActor with Stash with NameGen {
  val uri: URI = buildURI(protocol).fold(throw _, identity)
  val config: Properties = protocol.config
  var session: ImapAsyncSession = null

  private def buildURI(protocol: ImapProtocol): Either[IllegalArgumentException, URI] =
    for {
      host <- hostValidate(protocol.host)
      uri <- Try(new URI(s"${protocol.protocol}://$host:${protocol.port}"))
        .filter(uri1 => uri1.getHost != null && uri1.getHost.nonEmpty)
        .toEither
        .left.map(_ => new IllegalArgumentException(s"Invalid URI: $uri"))
    } yield uri

  private def hostValidate(host: String): Either[IllegalArgumentException, String] =
    host match {
      case null => scala.Left(new IllegalArgumentException("host is null"))
      case h if h.isEmpty => scala.Left(new IllegalArgumentException("host is empty"))
      case h if h.contains("_") => scala.Left(new IllegalArgumentException("host contains underscore: " + host))
      case _ => scala.Right(host)
    }

  override def receive: Receive = disconnected

  def disconnected: Receive = {
    case Command.Connect(userId) =>
      logger.debug(s"got connect request, $userId connecting to $uri")

      val config = new ImapAsyncSessionConfig
      config.setConnectionTimeoutMillis(50000)
      config.setReadTimeoutMillis(60000)
      val sniNames = null
      val localAddress = null

      session = client
        .createSession(uri, config, localAddress, sniNames, DebugMode.DEBUG_OFF, "ImapSession", ImapSession.sslContext)
        .get()
        .getSession

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
    case cmd@Command.Search(_, _, _) =>
      val handler = context.actorOf(SearchHandler.props(session), genName("search"))
      handler forward cmd
    case cmd@Command.UIDSearch(_, _, _) =>
      val handler = context.actorOf(UIDSearchHandler.props(session), genName("uidSearch"))
      handler forward cmd
    case cmd@Command.Select(_, _) =>
      val handler = context.actorOf(SelectHandler.props(session), genName("select"))
      handler forward cmd
    case cmd@Command.Capability(_) =>
      val handler = context.actorOf(CapabilityHandler.props(session), genName("capability"))
      handler forward cmd
    case cmd@Command.Check(_) =>
      val handler = context.actorOf(CheckHandler.props(session), genName("check"))
      handler forward cmd
    case cmd@Command.Close(_) =>
      val handler = context.actorOf(CloseHandler.props(session), genName("close"))
      handler forward cmd
    case cmd@Command.Enable(_, _) =>
      val handler = context.actorOf(EnableHandler.props(session), genName("enable"))
      handler forward cmd
    case cmd@Command.GetAcl(_, _) =>
      val handler = context.actorOf(GetAclHandler.props(session), genName("getAcl"))
      handler forward cmd
    case cmd@Command.GetQuotaRoot(_, _) =>
      val handler = context.actorOf(GetQuotaRootHandler.props(session), genName("getQuotaRoot"))
      handler forward cmd
    case cmd@Command.GetQuota(_, _) =>
      val handler = context.actorOf(GetQuotaHandler.props(session), genName("getQuota"))
      handler forward cmd
    case cmd@Command.SetQuota(_, _) =>
      val handler = context.actorOf(SetQuotaHandler.props(session), genName("setQuota"))
      handler forward cmd
    case cmd@Command.Compress(_) =>
      val handler = context.actorOf(CompressHandler.props(session), genName("compress"))
      handler forward cmd
    case cmd@Command.Idle(_) =>
      val handler = context.actorOf(IdleHandler.props(session), genName("idle"))
      handler forward cmd
    case cmd@Command.Logout(_) =>
      val handler = context.actorOf(LogoutHandler.props(session), genName("logout"))
      handler forward cmd
    case cmd@Command.Lsub(_, _, _) =>
      val handler = context.actorOf(LsubHandler.props(session), genName("lsub"))
      handler forward cmd
    case cmd@Command.MyRights(_, _) =>
      val handler = context.actorOf(MyRightsHandler.props(session), genName("myrights"))
      handler forward cmd
    case cmd@Command.Noop(_) =>
      val handler = context.actorOf(NoopHandler.props(session), genName("noop"))
      handler forward cmd
    case cmd@Command.Namespace(_) =>
      val handler = context.actorOf(NamespaceHandler.props(session), genName("namespace"))
      handler forward cmd
    case cmd@Command.Subscribe(_, _) =>
      val handler = context.actorOf(SubscribeHandler.props(session), genName("subscribe"))
      handler forward cmd
    case cmd@Command.Unsubscribe(_, _) =>
      val handler = context.actorOf(UnsubscribeHandler.props(session), genName("unsubscribe"))
      handler forward cmd
    case cmd@Command.CreateFolder(_, _) =>
      val handler = context.actorOf(CreateFolderHandler.props(session), genName("createFolder"))
      handler forward cmd
    case cmd@Command.DeleteFolder(_, _) =>
      val handler = context.actorOf(DeleteFolderHandler.props(session), genName("deleteFolder"))
      handler forward cmd
    case cmd@Command.RenameFolder(_, _, _) =>
      val handler = context.actorOf(RenameFolderHandler.props(session), genName("renameFolder"))
      handler forward cmd
    case cmd@Command.ExamineFolder(_, _) =>
      val handler = context.actorOf(ExamineFolderHandler.props(session), genName("examineFolder"))
      handler forward cmd
    case cmd@Command.Move(_, _, _) =>
      val handler = context.actorOf(MoveHandler.props(session), genName("move"))
      handler forward cmd
    case cmd@Command.Copy(_, _, _) =>
      val handler = context.actorOf(CopyHandler.props(session), genName("copy"))
      handler forward cmd
    case cmd@Command.UidCopy(_, _, _) =>
      val handler = context.actorOf(UidCopyHandler.props(session), genName("uidCopy"))
      handler forward cmd
    case cmd@Command.UidMove(_, _, _) =>
      val handler = context.actorOf(UidMoveHandler.props(session), genName("uidMove"))
      handler forward cmd
    case cmd@Command.UidExpunge(_, _) =>
      val handler = context.actorOf(UidExpungeHandler.props(session), genName("uidExpunge"))
      handler forward cmd
    case cmd@Command.Status(_, _, _) =>
      val handler = context.actorOf(StatusHandler.props(session), genName("status"))
      handler forward cmd
    case cmd@Command.Unselect(_) =>
      val handler = context.actorOf(UnselectHandler.props(session), genName("unselect"))
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
    case cmd@Command.UidStore(_, _, _) =>
      val handler = context.actorOf(UidStoreHandler.props(session), genName("uidStore"))
      handler forward cmd
    case cmd@Command.Expunge(_) =>
      val handler = context.actorOf(ExpungeHandler.props(session), genName("expunge"))
      handler forward cmd
    case cmd@Command.Append(_, _, _, _, _) =>
      val handler = context.actorOf(AppendHandler.props(session), genName("append"))
      handler forward cmd
    case Command.Disconnect(userId) =>
      context.become(disconnected)
      val responseCallback: Consumer[java.lang.Boolean] = _ => {
        session = null
        sender() ! Response.Disconnected(s"Disconnected command for ${userId.value}")
      }
      val future = session.close()
      future.setDoneCallback(responseCallback)
    case message =>
      logger.error(s"connected - got unexpected message $message")

  }
}

case class ImapStateError(msg: String) extends IllegalStateException(msg) with NoStackTrace

