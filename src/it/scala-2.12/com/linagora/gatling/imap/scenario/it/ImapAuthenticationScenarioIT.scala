package com.linagora.gatling.imap.scenario.it

import com.linagora.gatling.imap.Fixture.bart
import com.linagora.gatling.imap.PreDef.imap
import com.linagora.gatling.imap.scenario.{ImapAuthenticationScenario, ImapCapabilityScenario, ImapCheckScenario, ImapCloseScenario, ImapCompressScenario, ImapCopyMessageScenario, ImapCreateFolderScenario, ImapDeleteFolderScenario, ImapEnableScenario, ImapExamineFolderScenario, ImapExpungeScenario, ImapGetAclScenario, ImapGetQuotaRootScenario, ImapGetQuotaScenario, ImapIdleScenario, ImapLogoutScenario, ImapLsubScenario, ImapMoveMessageScenario, ImapMyRightsScenario, ImapNamespaceScenario, ImapNoopScenario, ImapRenameFolderScenario, ImapSearchScenario, ImapSimpleScenario, ImapStatusScenario, ImapSubscribeScenario, ImapUIDFetchScenario, ImapUidCopyMessageScenario, ImapUidExpungeMessageScenario, ImapUidMoveMessageScenario, ImapUidStoreScenario, ImapUnselectScenario, ImapUnsubscribeScenario, MassiveOperationScenario}
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

class ImapMassiveOperationScenarioIT extends BaseIt(CyrusServer.start()) {
  scenario(MassiveOperationScenario(_))
}

class ImapMassiveOperationScenarioJamesIT extends BaseIt(JamesServer.start()) {
  scenario(MassiveOperationScenario(_))
}
//
//class ImapAuthenticationScenarioIT extends BaseIt(CyrusServer.start()) {
//  scenario(ImapAuthenticationScenario(_))
//}
//
//class ImapExpungeScenarioIT extends BaseIt(CyrusServer.start()) {
//  scenario(ImapExpungeScenario(_))
//}
//
//class ImapSimpleScenarioIT extends BaseIt(CyrusServer.start()) {
//  scenario(ImapSimpleScenario(_))
//}
//
//class ImapUIDFetchScenarioIT extends BaseIt(CyrusServer.start()) {
//  scenario(ImapUIDFetchScenario(_))
//}
//
//class ImapUidStoreScenarioIT extends BaseIt(CyrusServer.start()) {
//  scenario(ImapUidStoreScenario(_))
//}
//
//class ImapAuthenticationScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapAuthenticationScenario(_))
//}
//
//class ImapExpungeScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapExpungeScenario(_))
//}
//
//class ImapSimpleScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapSimpleScenario(_))
//}
//
//class ImapUIDFetchScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapUIDFetchScenario(_))
//}
//
//class ImapCapabilityScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapCapabilityScenario(_))
//}
//
//class ImapCheckScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapCheckScenario(_))
//}
//
//class ImapCloseScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapCloseScenario(_))
//}
//
//class ImapEnableScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapEnableScenario(_))
//}
//
//class ImapGetAclScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapGetAclScenario(_))
//}
//
//class ImapGetQuotaRootScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapGetQuotaRootScenario(_))
//}
//
//class ImapGetQuotaScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapGetQuotaScenario(_))
//}
//
//class ImapCompressScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapCompressScenario(_))
//}
//
//class ImapIdleScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapIdleScenario(_))
//}
//
//class ImapLogoutScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapLogoutScenario(_))
//}
//
//class ImapLsubScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapLsubScenario(_))
//}
//
//class ImapMyRightsScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapMyRightsScenario(_))
//}
//
//class ImapNamespaceScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapNamespaceScenario(_))
//}
//
//class ImapSubscribeScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapSubscribeScenario(_))
//}
//
//class ImapUnsubscribeScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapUnsubscribeScenario(_))
//}
//
//class ImapCreateFolderScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapCreateFolderScenario(_))
//}
//
//class ImapDeleteFolderScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapDeleteFolderScenario(_))
//}
//
//class ImapRenameFolderScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapRenameFolderScenario(_))
//}
//
//class ImapExamineFolderScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapExamineFolderScenario(_))
//}
//
//class ImapMoveScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapMoveMessageScenario(_))
//}
//
//class ImapCopyScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapCopyMessageScenario(_))
//}
//
//class ImapUidCopyScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapUidCopyMessageScenario(_))
//}
//
//class ImapUidMoveScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapUidMoveMessageScenario(_))
//}
//
//class ImapUidExpungeScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapUidExpungeMessageScenario(_))
//}
//
//class ImapNoopScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapNoopScenario(_))
//}
//
//class ImapStatusScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapStatusScenario(_))
//}
//
//class ImapUnselectScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapUnselectScenario(_))
//}
//
//class ImapSearchtScenarioJamesIT extends BaseIt(JamesServer.start()) {
//  scenario(ImapSearchScenario(_))
//}