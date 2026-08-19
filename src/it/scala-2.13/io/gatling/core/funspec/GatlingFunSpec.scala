package io.gatling.core.funspec

import scala.collection.mutable.ListBuffer

import io.gatling.core.Predef._
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure.ChainBuilder

// ponytail: Gatling dropped GatlingFunSpec in 3.12; backported here so the existing
// scenario-based integration specs keep working unmodified.
abstract class GatlingFunSpec extends Simulation {

  def protocolConf: Protocol

  def spec(actionBuilder: ActionBuilder): ListBuffer[ActionBuilder] = specs += actionBuilder

  private[this] val specs = new ListBuffer[ActionBuilder]

  private[this] lazy val testScenario = scenario(this.getClass.getSimpleName)
    .exec(new ChainBuilder(specs.reverse.toList))

  private def setupRegisteredSpecs(): Unit = {
    require(specs.nonEmpty, "At least one spec needs to be defined")
    setUp(testScenario.inject(atOnceUsers(1)))
      .protocols(protocolConf)
      .assertions(forAll.failedRequests.percent.is(0))
  }

  override private[gatling] def params(configuration: GatlingConfiguration) = {
    setupRegisteredSpecs()
    super.params(configuration)
  }
}