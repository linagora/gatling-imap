package com.linagora.gatling.imap.action

import akka.actor.Props
import com.linagora.gatling.imap.check.ImapCheck
import com.linagora.gatling.imap.protocol.command.{FetchAttributes, FetchRange}
import com.linagora.gatling.imap.protocol.{ImapComponents, ImapProtocol}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.{Action, ExitableActorDelegatingAction}
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen

import scala.collection.immutable.Seq

class ImapActionBuilder(requestName: String) {
  def login(user: Expression[String], password: Expression[String]): ImapLoginActionBuilder = {
    ImapLoginActionBuilder(requestName, user, password, Seq.empty)
  }

  def select(mailbox: Expression[String]): ImapSelectActionBuilder = {
    ImapSelectActionBuilder(requestName, mailbox, Seq.empty)
  }

  def list(reference: Expression[String], name: Expression[String]): ImapListActionBuilder = {
    ImapListActionBuilder(requestName, reference, name, Seq.empty)
  }

  def fetch(sequence: Expression[Seq[FetchRange]], attributes: Expression[FetchAttributes]): ImapFetchActionBuilder = {
    ImapFetchActionBuilder(requestName, sequence, attributes, Seq.empty)
  }

  def connect(): ImapConnectActionBuilder = {
    ImapConnectActionBuilder(requestName)
  }
}

abstract class ImapCommandActionBuilder extends ActionBuilder with NameGen {
  def props(ctx: ImapActionContext): Props

  def requestName: String

  def actionName: String

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val components: ImapComponents = ctx.protocolComponentsRegistry.components(ImapProtocol.ImapProtocolKey)
    val imapCtx = ImapActionContext(components.sessions, ctx.coreComponents.statsEngine, next)
    val actionActor = ctx.system.actorOf(props(imapCtx), actionName)
    new ExitableActorDelegatingAction(genName(requestName), ctx.coreComponents.statsEngine, next, actionActor)
  }

}

case class ImapLoginActionBuilder(requestName: String, username: Expression[String], password: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapLoginActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    LoginAction.props(ctx, requestName, checks, username, password)

  override val actionName: String = "login-action"
}

case class ImapSelectActionBuilder(requestName: String, mailbox: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapSelectActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    SelectAction.props(ctx, requestName, checks, mailbox)

  override val actionName: String = "select-action"
}

case class ImapListActionBuilder(requestName: String, reference: Expression[String], name: Expression[String], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapListActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    ListAction.props(ctx, requestName, checks, reference, name)

  override val actionName: String = "list-action"
}

case class ImapFetchActionBuilder(requestName: String, sequence: Expression[Seq[FetchRange]], attributes: Expression[FetchAttributes], private val checks: Seq[ImapCheck]) extends ImapCommandActionBuilder {
  def check(checks: ImapCheck*): ImapFetchActionBuilder = copy(checks = this.checks ++ checks)

  override def props(ctx: ImapActionContext): Props =
    FetchAction.props(ctx, requestName, checks, sequence, attributes)

  override val actionName: String = "fetch-action"
}

case class ImapConnectActionBuilder(requestName: String) extends ImapCommandActionBuilder {
  override def props(ctx: ImapActionContext): Props =
    ConnectAction.props(ctx, requestName)

  override val actionName: String = "connect-action"
}