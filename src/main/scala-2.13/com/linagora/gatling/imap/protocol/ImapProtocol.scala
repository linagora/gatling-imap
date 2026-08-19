package com.linagora.gatling.imap.protocol

import java.net.URI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

import com.linagora.gatling.imap.protocol.ImapComponents.SslContexts
import com.yahoo.imapnio.async.client.ImapAsyncSession.DebugMode
import com.yahoo.imapnio.async.client.{ImapAsyncClient, ImapAsyncSession, ImapAsyncSessionConfig}
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolComponents, ProtocolKey}
import io.gatling.core.session.Session

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NoStackTrace

object ImapProtocol {
  val ImapProtocolKey = new ProtocolKey[ImapProtocol, ImapComponents] {

    override def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[ImapProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): ImapProtocol = throw new IllegalStateException("Can't provide a default value for ImapProtocol")

    override def newComponents(coreComponents: CoreComponents): ImapProtocol => ImapComponents = { protocol =>
      ImapComponents(protocol)
    }
  }
}

case class ImapProtocol(host: String,
                        port: Int = 143,
                        protocol: String = "imap",
                        config: Properties = new Properties()
                       ) extends Protocol

object ImapComponents {
  def apply(protocol: ImapProtocol): ImapComponents =
    new ImapComponents(protocol, new ImapAsyncClient(8), new ConcurrentHashMap[Long, ImapAsyncSession]())

  object SslContexts {
    private val trustAllCerts: Array[javax.net.ssl.TrustManager] = Array[javax.net.ssl.TrustManager](new javax.net.ssl.X509TrustManager() {
      override def getAcceptedIssuers: Array[X509Certificate] = new Array[X509Certificate](0)
      override def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
      override def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
    })

    val trustAll: javax.net.ssl.SSLContext = {
      val sc = javax.net.ssl.SSLContext.getInstance("SSL")
      sc.init(null, trustAllCerts, new SecureRandom)
      sc
    }
  }
}

class ImapComponents(val protocol: ImapProtocol, val client: ImapAsyncClient, val userSessions: ConcurrentHashMap[Long, ImapAsyncSession]) extends ProtocolComponents {

  // ponytail: dedicated executor for the blocking createSession call so we don't
  // stall Gatling's event loop threads on connect. Per-account executors if throughput matters.
  private implicit val connectEc: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  override def onStart: Session => Session = s => s

  override def onExit: Session => Unit = session => disconnect(UserId(session.userId))

  def connect(userId: UserId)(onSuccess: ImapAsyncSession => Unit, onError: Exception => Unit): Unit = {
    Future {
      val session = createSession()
      userSessions.put(userId.value, session)
      session
    }.onComplete {
      case scala.util.Success(s) => onSuccess(s)
      case scala.util.Failure(e: Exception) => onError(e)
      case scala.util.Failure(other) => onError(new IllegalStateException(other))
    }
  }

  def sessionFor(userId: UserId): ImapAsyncSession = userSessions.get(userId.value)

  def disconnect(userId: UserId): Unit = {
    val session = userSessions.remove(userId.value)
    if (session != null) session.close()
  }

  private def createSession(): ImapAsyncSession = {
    val uri = buildURI(protocol).fold(throw _, identity)
    val config = new ImapAsyncSessionConfig
    config.setConnectionTimeoutMillis(50000)
    config.setReadTimeoutMillis(60000)
    val sniNames = null
    val localAddress = null
    client
      .createSession(uri, config, localAddress, sniNames, DebugMode.DEBUG_OFF, "ImapSession", SslContexts.trustAll)
      .get()
      .getSession
  }

  private def buildURI(protocol: ImapProtocol): Either[IllegalArgumentException, URI] =
    for {
      host <- hostValidate(protocol.host)
      uri <- Try(new URI(s"${protocol.protocol}://$host:${protocol.port}"))
        .filter(uri1 => uri1.getHost != null && uri1.getHost.nonEmpty)
        .toEither
        .left.map(_ => new IllegalArgumentException(s"Invalid URI: $protocol"))
    } yield uri

  private def hostValidate(host: String): Either[IllegalArgumentException, String] =
    host match {
      case null => scala.Left(new IllegalArgumentException("host is null"))
      case h if h.isEmpty => scala.Left(new IllegalArgumentException("host is empty"))
      case h if h.contains("_") => scala.Left(new IllegalArgumentException("host contains underscore: " + host))
      case _ => scala.Right(host)
    }
}

case class ImapStateError(msg: String) extends IllegalStateException(msg) with NoStackTrace