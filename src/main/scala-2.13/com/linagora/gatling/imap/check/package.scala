package com.linagora.gatling.imap

import com.linagora.gatling.imap.protocol.ImapResponses
import io.gatling.core.check.Check

package object check {
  type ImapCheck = Check[ImapResponses]
}