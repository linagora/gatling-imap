package com.linagora.gatling.imap.check

import com.linagora.gatling.imap.protocol.ImapResponses
import io.gatling.commons.validation.{Failure, Validation}
import io.gatling.core.check.Check.PreparedCache
import io.gatling.core.check.CheckResult
import io.gatling.core.session.Session

object ImapSimpleCheck {
  val DefaultMessage = "Imap check failed"
}

case class ImapSimpleCheck(validate: ImapResponses => Boolean, errorMessage: ImapResponses => String = _ => ImapSimpleCheck.DefaultMessage) extends ImapCheck {
  override def check(response: ImapResponses, session: Session, preparedCache: PreparedCache): Validation[CheckResult] =
    if (validate(response))
      CheckResult.NoopCheckResultSuccess
    else
      Failure(errorMessage(response))
}
