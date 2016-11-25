package com.linagora.gatling.imap.check

import io.gatling.commons.validation.Failure
import io.gatling.core.check.CheckResult

trait ImapCheckSupport {
  def ok = ImapSimpleCheck(_.isOk)

  def bad = ImapSimpleCheck(_.isBad, _ => "Failed to find expected bad status")

  def hasRecent(expected: Int) = ImapValidationCheck { responses =>
    responses.countRecent match {
      case Some(count) if count == expected => CheckResult.NoopCheckResultSuccess
      case Some(count) => Failure(s"Expected $expected recent messages but got $count")
      case None => Failure(s"Imap protocol violation : no valid RECENT response received")
    }
  }

  def hasFolder(expected: String) =
    ImapSimpleCheck(
      _.folderList.contains(expected),
      resp => s"""Unable to find folder '$expected' in ${resp.folderList.mkString(", ")}""")

  def hasNoRecent = hasRecent(0)
}