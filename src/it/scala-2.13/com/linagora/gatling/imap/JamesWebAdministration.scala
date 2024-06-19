package com.linagora.gatling.imap

import java.net.URL

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.linagora.gatling.imap.protocol.{Domain, User}
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JamesWebAdministration(val baseUrl: URL) {
  // Create Akka system for thread and streaming management
  implicit val system: ActorSystem = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()

  def addDomain(domain: Domain): Future[Domain] = wsClient.url(s"$baseUrl/domains/${domain.value}")
    .put("")
    .map(response => domain)

  def addUser(user: User): Future[User] =
    wsClient.url(s"$baseUrl/users/${user.login}")
      .put(s"""{"password":"${user.password}"}""")
      .map(response => user)

}
