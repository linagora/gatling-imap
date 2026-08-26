package io.gatling.core.funspec

import scala.collection.mutable.ListBuffer

import io.gatling.core.Predef._
import io.gatling.core.action.builder.Executable
import io.gatling.core.protocol.Protocol
import io.gatling.core.scenario.SimulationParams

// ponytail: Gatling dropped GatlingFunSpec in 3.12; backported here so the existing
// scenario-based integration specs keep working unmodified.
abstract class GatlingFunSpec extends Simulation {

  def protocolConf: Protocol

  def spec(executable: Executable): ListBuffer[Executable] = specs += executable

  private[this] val specs = new ListBuffer[Executable]

  private[this] lazy val testScenario = scenario(this.getClass.getSimpleName)
    .exec(specs.toList)

  private def setupRegisteredSpecs(): Unit = {
    require(specs.nonEmpty, "At least one spec needs to be defined")
    setUp(testScenario.inject(atOnceUsers(1)))
      .protocols(protocolConf)
      .assertions(forAll.failedRequests.percent.is(0))
  }

  override private[gatling] def params: SimulationParams = {
    setupRegisteredSpecs()
    super.params
  }
}
