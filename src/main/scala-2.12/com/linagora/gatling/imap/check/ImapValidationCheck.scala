package com.linagora.gatling.imap.check

import com.linagora.gatling.imap.protocol.ImapResponses
import io.gatling.commons.validation.Validation
import io.gatling.core.Predef.Session
import io.gatling.core.check.Check.PreparedCache
import io.gatling.core.check.CheckResult

case class ImapValidationCheck(validate: ImapResponses => Validation[CheckResult], message: String = ImapSimpleCheck.DefaultMessage) extends ImapCheck {
  override def check(response: ImapResponses, session: Session, preparedCache: PreparedCache): Validation[CheckResult] =
    validate(response)

}
