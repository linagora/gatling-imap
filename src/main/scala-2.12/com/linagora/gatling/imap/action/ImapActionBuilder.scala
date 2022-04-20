package com.linagora.gatling.imap.action

import java.util.Calendar

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.command.{FetchAttributes, MessageRanges, StoreFlags}
import com.linagora.gatling.imap.protocol.{ImapComponents, ImapProtocol, StatusItems}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.{Action, ExitableActorDelegatingAction}
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen

import scala.collection.immutable.Seq

class ImapActionBuilder(requestName: String) {
  def login(user: Expression[String], password: Expression[String]): ImapLoginActionBuilder =
    ImapLoginActionBuilder(requestName, user, password, Seq.empty)

  def capability(): ImapCapabilityActionBuilder =
    ImapCapabilityActionBuilder(requestName, Seq.empty)

  def noop(): ImapNoopActionBuilder =
    ImapNoopActionBuilder(requestName, Seq.empty)

  def check(): ImapCheckActionBuilder =
    ImapCheckActionBuilder(requestName, Seq.empty)

  def close(): ImapCloseActionBuilder =
    ImapCloseActionBuilder(requestName, Seq.empty)

  def logout(): ImapLogoutActionBuilder =
    ImapLogoutActionBuilder(requestName, Seq.empty)

  def idle(): ImapIdleActionBuilder =
    ImapIdleActionBuilder(requestName, Seq.empty)

  def unselect(): ImapUnselectActionBuilder =
    ImapUnselectActionBuilder(requestName, Seq.empty)

  def select(mailbox: Expression[String]): ImapSelectActionBuilder =
    ImapSelectActionBuilder(requestName, mailbox, Seq.empty)

  def getQuotaRoot(mailbox: Expression[String]): ImapGetQuotaRootActionBuilder =
    ImapGetQuotaRootActionBuilder(requestName, mailbox, Seq.empty)

  def getAcl(mailbox: Expression[String]): ImapGetAclActionBuilder =
    ImapGetAclActionBuilder(requestName, mailbox, Seq.empty)

  def myRights(mailbox: Expression[String]): ImapMyRightsActionBuilder =
    ImapMyRightsActionBuilder(requestName, mailbox, Seq.empty)

  def enable(capability: Expression[String]): ImapEnableActionBuilder =
    ImapEnableActionBuilder(requestName, capability, Seq.empty)

  def list(reference: Expression[String], name: Expression[String]): ImapListActionBuilder =
    ImapListActionBuilder(requestName, reference, name, Seq.empty)

  def lsub(reference: Expression[String], name: Expression[String]): ImapLsubActionBuilder =
    ImapLsubActionBuilder(requestName, reference, name, Seq.empty)

  def fetch(sequence: Expression[MessageRanges], attributes: Expression[FetchAttributes]): ImapFetchActionBuilder =
    ImapFetchActionBuilder(requestName, sequence, attributes, Seq.empty)

  def uidFetch(sequence: Expression[MessageRanges], attributes: Expression[FetchAttributes]): ImapUIDFetchActionBuilder =
    ImapUIDFetchActionBuilder(requestName, sequence, attributes, Seq.empty)

  def store(sequence: Expression[MessageRanges], flags: Expression[StoreFlags]): ImapStoreActionBuilder =
    ImapStoreActionBuilder(requestName, sequence, flags, Seq.empty)

  def status(mailbox: Expression[String], items: Expression[StatusItems]): ImapStatusActionBuilder =
    ImapStatusActionBuilder(requestName, mailbox, items, Seq.empty)

  def expunge(): ImapExpungeActionBuilder =
    ImapExpungeActionBuilder(requestName, Seq.empty)

  def append(mailbox: Expression[String], flags: Expression[Option[Seq[String]]], date: Expression[Option[Calendar]], content: Expression[String]): ImapAppendActionBuilder =
    ImapAppendActionBuilder(requestName, mailbox, flags, date, content, Seq.empty)

  def connect(): ImapConnectActionBuilder =
    ImapConnectActionBuilder(requestName)
}

abstract class ImapCommandActionBuilder extends ActionBuilder with NameGen {
  def props(ctx: ImapActionContext): Props

  def requestName: String

  def actionName: String

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val components: ImapComponents = ctx.protocolComponentsRegistry.components(ImapProtocol.ImapProtocolKey)
    val imapCtx = ImapActionContext(ctx.coreComponents.clock, components.sessions, ctx.coreComponents.statsEngine, next)
    val actionActor = ctx.coreComponents.actorSystem.actorOf(props(imapCtx), genName(actionName))
    new ExitableActorDelegatingAction(genName(requestName), ctx.coreComponents.statsEngine, ctx.coreComponents.clock, next, actionActor)
  }

}

case class ImapLoginActionBuilder(requestName: String, username: Expression[String], password: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapLoginActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    LoginAction.props(ctx, requestName, checks, username, password)

  override val actionName: String = "login-action"
}

case class ImapCapabilityActionBuilder(requestName: String, private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapCapabilityActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    CapabilityAction.props(ctx, requestName, checks)

  override val actionName: String = "capability-action"
}

case class ImapNoopActionBuilder(requestName: String, private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapNoopActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    NoopAction.props(ctx, requestName, checks)

  override val actionName: String = "noop-action"
}

case class ImapCheckActionBuilder(requestName: String, private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapCheckActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    CheckAction.props(ctx, requestName, checks)

  override val actionName: String = "check-action"
}

case class ImapCloseActionBuilder(requestName: String, private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapCloseActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    CloseAction.props(ctx, requestName, checks)

  override val actionName: String = "close-action"
}

case class ImapLogoutActionBuilder(requestName: String, private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapLogoutActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    LogoutAction.props(ctx, requestName, checks)

  override val actionName: String = "logout-action"
}

case class ImapIdleActionBuilder(requestName: String, private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapIdleActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    IdleAction.props(ctx, requestName, checks)

  override val actionName: String = "idle-action"
}

case class ImapUnselectActionBuilder(requestName: String, private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapUnselectActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    UnselectAction.props(ctx, requestName, checks)

  override val actionName: String = "unselect-action"
}

case class ImapSelectActionBuilder(requestName: String, mailbox: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapSelectActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    SelectAction.props(ctx, requestName, checks, mailbox)

  override val actionName: String = "select-action"
}

case class ImapStatusActionBuilder(requestName: String, mailbox: Expression[String], items: Expression[StatusItems], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapStatusActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    StatusAction.props(ctx, requestName, checks, mailbox, items)

  override val actionName: String = "status-action"
}

case class ImapEnableActionBuilder(requestName: String, capability: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapEnableActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    EnableAction.props(ctx, requestName, checks, capability)

  override val actionName: String = "enable-action"
}

case class ImapGetQuotaRootActionBuilder(requestName: String, capability: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapGetQuotaRootActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    GetQuotaRootAction.props(ctx, requestName, checks, capability)

  override val actionName: String = "get-quota-root-action"
}

case class ImapGetAclActionBuilder(requestName: String, capability: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapGetAclActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    GetAclAction.props(ctx, requestName, checks, capability)

  override val actionName: String = "get-acl-action"
}

case class ImapMyRightsActionBuilder(requestName: String, mailbox: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapMyRightsActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    MyRightsAction.props(ctx, requestName, checks, mailbox)

  override val actionName: String = "my-rights-action"
}

case class ImapListActionBuilder(requestName: String, reference: Expression[String], name: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapListActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    ListAction.props(ctx, requestName, checks, reference, name)

  override val actionName: String = "list-action"
}

case class ImapLsubActionBuilder(requestName: String, reference: Expression[String], name: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapLsubActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    LsubAction.props(ctx, requestName, checks, reference, name)

  override val actionName: String = "list-action"
}

case class ImapFetchActionBuilder(requestName: String, sequence: Expression[MessageRanges], attributes: Expression[FetchAttributes], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapFetchActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    FetchAction.props(ctx, requestName, checks, sequence, attributes)

  override val actionName: String = "fetch-action"
}

case class ImapUIDFetchActionBuilder(requestName: String, sequence: Expression[MessageRanges], attributes: Expression[FetchAttributes], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapUIDFetchActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    UIDFetchAction.props(ctx, requestName, checks, sequence, attributes)

  override val actionName: String = "uid-fetch-action"
}

case class ImapStoreActionBuilder(requestName: String, sequence: Expression[MessageRanges], flags: Expression[StoreFlags], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapStoreActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    StoreAction.props(ctx, requestName, checks, sequence, flags)

  override val actionName: String = "store-action"
}

case class ImapExpungeActionBuilder(requestName: String, private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapExpungeActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    ExpungeAction.props(ctx, requestName, checks)

  override val actionName: String = "expunge-action"
}

case class ImapAppendActionBuilder(requestName: String, mailbox: Expression[String], flags: Expression[Option[Seq[String]]], date: Expression[Option[Calendar]], content: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapAppendActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    AppendAction.props(ctx, requestName, checks, mailbox, flags, date, content)

  override val actionName: String = "append-action"
}

case class ImapConnectActionBuilder(requestName: String) extends ImapCommandActionBuilder {
  override def props(ctx: ImapActionContext): Props =
    ConnectAction.props(ctx, requestName)

  override val actionName: String = "connect-action"
}