package com.linagora.gatling.imap.check

import com.linagora.gatling.imap.protocol.ImapResponses
import io.gatling.commons.validation.Validation
import io.gatling.core.Predef.Session
import io.gatling.core.check.CheckResult

import scala.collection.mutable

case class ImapValidationCheck(validate: ImapResponses => Validation[CheckResult], message: String = ImapSimpleCheck.DefaultMessage) extends ImapCheck {
  override def check(responses: ImapResponses, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = {
    validate(responses)
  }
}
