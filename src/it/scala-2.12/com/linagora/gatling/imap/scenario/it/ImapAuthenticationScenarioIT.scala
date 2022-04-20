package com.linagora.gatling.imap.scenario.it

import com.linagora.gatling.imap.Fixture.bart
import com.linagora.gatling.imap.PreDef.imap
import com.linagora.gatling.imap.scenario.{ImapAuthenticationScenario, ImapCapabilityScenario, ImapCheckScenario, ImapCloseScenario, ImapEnableScenario, ImapExpungeScenario, ImapGetAclScenario, ImapGetQuotaRootScenario, ImapIdleScenario, ImapLogoutScenario, ImapLsubScenario, ImapMyRightsScenario, ImapNoopScenario, ImapSimpleScenario, ImapStatusScenario, ImapUIDFetchScenario, ImapUnselectScenario}
import com.linagora.gatling.imap.{CyrusServer, Fixture, JamesServer, RunningServer}
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.funspec.GatlingFunSpec
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure.ScenarioBuilder
import org.slf4j
import org.slf4j.LoggerFactory

abstract class BaseIt(server: RunningServer) extends GatlingFunSpec {
  val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

  lazy val protocolConf: Protocol = imap.host("localhost").port(server.mappedImapPort()).build()
  before(server.addDomain(Fixture.simpson))
  before(server.addUser(bart))
  after(server.stop())

  protected def scenario(scenario: FeederBuilder => ScenarioBuilder) = {
    scenario(Fixture.feederBuilder(bart)).actionBuilders.reverse.foreach(
      spec _
    )
  }
}


class ImapAuthenticationScenarioIT extends BaseIt(CyrusServer.start()) {
  scenario(ImapAuthenticationScenario(_))
}

class ImapExpungeScenarioIT extends BaseIt(CyrusServer.start()) {
  scenario(ImapExpungeScenario(_))
}

class ImapSimpleScenarioIT extends BaseIt(CyrusServer.start()) {
  scenario(ImapSimpleScenario(_))
}

class ImapUIDFetchScenarioIT extends BaseIt(CyrusServer.start()) {
  scenario(ImapUIDFetchScenario(_))
}


class ImapAuthenticationScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapAuthenticationScenario(_))
}

class ImapExpungeScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapExpungeScenario(_))
}

class ImapSimpleScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapSimpleScenario(_))
}

class ImapUIDFetchScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapUIDFetchScenario(_))
}

class ImapCapabilityScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapCapabilityScenario(_))
}

class ImapCheckScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapCheckScenario(_))
}

class ImapCloseScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapCloseScenario(_))
}

class ImapEnableScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapEnableScenario(_))
}

class ImapGetAclScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapGetAclScenario(_))
}

class ImapGetQuotaRootScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapGetQuotaRootScenario(_))
}

class ImapIdleScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapIdleScenario(_))
}

class ImapLogoutScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapLogoutScenario(_))
}

class ImapLsubScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapLsubScenario(_))
}

class ImapMyRightsScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapMyRightsScenario(_))
}

class ImapNoopScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapNoopScenario(_))
}

class ImapStatusScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapStatusScenario(_))
}

class ImapUnselectScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(ImapUnselectScenario(_))
}