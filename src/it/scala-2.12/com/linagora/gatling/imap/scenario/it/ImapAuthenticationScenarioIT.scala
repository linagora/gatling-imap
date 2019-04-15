package com.linagora.gatling.imap.scenario.it

import com.linagora.gatling.imap.Fixture.bart
import com.linagora.gatling.imap.PreDef.imap
import com.linagora.gatling.imap.scenario.{ImapAuthenticationScenario, ImapExpungeScenario, ImapSimpleScenario, ImapUIDFetchScenario}
import com.linagora.gatling.imap.{CyrusServer, Fixture, RunningServer}
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.funspec.GatlingFunSpec
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure.ScenarioBuilder
import org.slf4j
import org.slf4j.LoggerFactory

abstract class BaseIt extends GatlingFunSpec {
  val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

  private val server: RunningServer = CyrusServer.start()
  lazy val protocolConf: Protocol = imap.host("localhost").port(server.mappedImapPort()).build()
  before(server.addUser(bart))
  after(server.stop())

  protected def scenario(scenario: FeederBuilder => ScenarioBuilder) = {
    scenario(Fixture.feederBuilder(bart)).actionBuilders.reverse.foreach(
      spec _
    )
  }
}

class ImapAuthenticationScenarioIT extends BaseIt {
  scenario(ImapAuthenticationScenario(_))
}

class ImapExpungeScenarioIT extends BaseIt {
  scenario(ImapExpungeScenario(_))
}

class ImapSimpleScenarioIT extends BaseIt {
  scenario(ImapSimpleScenario(_))
}

class ImapUIDFetchScenarioIT extends BaseIt {
  scenario(ImapUIDFetchScenario(_))
}