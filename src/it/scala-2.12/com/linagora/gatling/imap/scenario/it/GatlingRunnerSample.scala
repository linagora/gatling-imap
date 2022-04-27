package com.linagora.gatling.imap.scenario.it

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object GatlingRunnerSample extends App {
  val simulationClass: String = "com.linagora.gatling.imap.scenario.it.ImapCompressScenarioJamesIT"
  val props: GatlingPropertiesBuilder = new GatlingPropertiesBuilder
  props.resourcesDirectory("src/main/scala-2.12")
  props.binariesDirectory("target/scala-2.12/classes")
  props.resultsDirectory("/tmp/gatling-result")
  props.simulationClass(simulationClass)

  Gatling.fromMap(props.build)
}
