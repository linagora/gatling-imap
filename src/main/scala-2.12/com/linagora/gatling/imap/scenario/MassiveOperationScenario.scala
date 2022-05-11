package com.linagora.gatling.imap.scenario

import java.util.Calendar

import com.linagora.gatling.imap.PreDef.{imap, ok, debug}
import com.linagora.gatling.imap.protocol.command.MessageRange.From
import com.linagora.gatling.imap.protocol.command.{MessageRanges, Silent, StoreFlags}
import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure._

import scala.collection.immutable.Seq
import scala.concurrent.duration._

object MassiveOperationScenario {
  private val gracePeriod = 5 milliseconds
  private val numberOfMailInInbox = 20000
  private val numberOfSubMailboxes = 1000
  private val mailboxAName = "user.cyrus.mailboxA"
  private val mailboxBName = "user.cyrus.mailboxB"
  private val mailboxCName = "user.cyrus.mailboxC"
  private val mailboxDName = "user.cyrus.mailboxD"
  private val mailboxDNewName = "user.cyrus.mailboxDNew"

  private val createMailboxA_B_C = exec(imap("createFolder").createFolder(mailboxAName).check(ok))
    .exec(imap("createFolder").createFolder(mailboxBName).check(ok))
    .exec(imap("createFolder").createFolder(mailboxCName).check(ok))
  private val populateMailboxA = repeat(numberOfMailInInbox)(pause(gracePeriod)
    .exec(imap("append").append(mailboxAName, Option.empty[Seq[String]], Option.empty[Calendar],
      """Return-Path: expeditor@linagora.com>
      |Delivered-To: ${username}
      |Received: from 172.17.0.1 (EHLO incoming.linagora.com) ([172.17.0.1])
      |          by incoming.linagora.com (JAMES SMTP Server ) with ESMTP ID e2de7b7e;
      |          Tue, 26 Apr 2022 02:27:56 +0000 (UTC)
      |Received: from smtp.linagora.com (smtp.linagora.com [172.17.0.1])
      |	by incoming.linagora.com (Postfix) with ESMTPS id 1AA739C621;
      |	Tue, 26 Apr 2022 02:27:56 +0000 (UTC)
      |Received: from ?Open?PaaS?SMTP?server?for?Linagora? (unknown [172.17.0.1])
      |	(using TLSv1.2 with cipher ECDHE-RSA-AES256-GCM-SHA384 (256/256 bits))
      |	(No client certificate requested)
      |	by smtp.linagora.com (Postfix) with ESMTPSA id 0E3494051E;
      |	Tue, 26 Apr 2022 04:27:56 +0200 (CEST)
      |DKIM-Signature: a=rsa-sha256; b=rpsVo5Aah1YYw7bP6vOJF8CCd3/jRXCrk3WARM4PMCUvliAbIUhhXIIN1bN84tOBKXqClx4HhYe63Lp2qbNbdrZFLdKAkhgfFf5/71OR+FbcTm78mnWP05HnY5ZMaAmmrhcIUVvDGhcg5NJSdmGf4WIpp2DqcUV7laXqNbC62vIcD5+gKLUEMGD1LudlHIEPA1MRfq/jRw94lPWX1yCNZaAvsoPG+oyedpbMyJ59MTQqz2WY5d0S1t4hTumrRHb/6kWHwV2ytzXMzZ9LITOYDtdEA44Bhr65dKAmWoOAvFVvC/lQP5dxkaFdx0vQ9Ei9Oc0r0tjg90YI6JLMm59e+A==; s=smtpoutjames; d=linagora.com; v=1; bh=phAFKC0vGk76uBUcy6pMhmSkmrTh/vYFrYkrvXUaDDQ=; h=from : reply-to : subject : date : to : cc : resent-date : resent-from : resent-sender : resent-to : resent-cc : in-reply-to : references : list-id : list-help : list-unsubscribe : list-subscribe : list-post : list-owner : list-archive;
      |MIME-Version: 1.0
      |Content-Type: text/html; charset=UTF-8
      |Content-Transfer-Encoding: quoted-printable
      |X-LINAGORA-Copy-Delivery-Done: 1
      |From: Tuan PHAM <expeditor@linagora.com>
      |Sender: Tuan PHAM <expeditor@linagora.com>
      |Reply-To: expeditor@linagora.com
      |To: Recipient <recipient@linagora.com
      |Message-ID: <Mime4j.3ae.98d4b510005bd7dc.18063b25b64@linagora.com>
      |Date: Tue, 26 Apr 2022 02:27:54 +0000
      |In-Reply-To: <Mime4j.3ad.3634707f65cfee8e.18063b16bc3@linagora.com>
      |Subject: test subject
      |
      |Dear ${username},
      |
      |Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum nunc sem, dignissim eu gravida in, ornare ac
      |lectus. Nunc porta tristique eros, non rhoncus felis euismod non. Aenean ut pulvinar sem. Fusce fermentum orci at
      |ex condimentum condimentum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Morbi consequat in sem
      |eu elementum. Pellentesque id justo ipsum. Phasellus ex odio, varius ut elit non, ultricies gravida neque. Quisque
      |sed maximus erat. Sed mi neque, gravida eget tincidunt eget, porta non nibh. Ut in mattis nisi. Morbi gravida augue
      |eget fringilla hendrerit.
      |
      |Sed tempus libero id nunc sagittis bibendum. Cras finibus venenatis nisi nec tincidunt. Praesent dignissim elit
      |vel lectus blandit lobortis. Vestibulum laoreet eget arcu non convallis. Nunc dignissim sapien vel nisi lacinia,
      |nec tincidunt dolor accumsan. Nulla eu quam quis erat lobortis ultricies. Sed dolor diam, facilisis ut dolor id,
      |tempus dictum lorem. Sed tempor dapibus ultrices. Sed scelerisque porta justo, sed sagittis magna finibus eu.
      |Duis lobortis et nisi non faucibus. Nunc posuere felis sit amet sem suscipit, vulputate tincidunt nisl aliquam.
      |Duis vel risus ornare, scelerisque arcu sagittis, tristique mauris.
      |
      |Integer eu nisl vitae diam mattis sollicitudin. Interdum et malesuada fames ac ante ipsum primis in faucibus.
      |Phasellus mollis, elit vitae ultrices commodo, leo ipsum mattis sem, ut viverra lacus tellus luctus risus. Cras
      |elit neque, viverra ut magna ac, tempus condimentum sem. Cras posuere congue congue. Duis sollicitudin erat eu
      |urna aliquet, eu cursus quam laoreet. Suspendisse a malesuada tortor, sed efficitur ante. Integer in metus vel
      |nulla dignissim gravida. Interdum et malesuada fames ac ante ipsum primis in faucibus. Integer nec iaculis massa.
      |Sed vitae auctor lorem.
      |
      |Vestibulum turpis augue, vulputate sit amet nibh sit amet, cursus elementum ipsum. Phasellus tellus nulla,
      |euismod in est nec, rhoncus luctus ex. Nullam sed sagittis ante, vel facilisis leo. Duis sem felis, placerat eget
      |commodo eget, molestie vel magna. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris dapibus sagittis
      |est, eget tincidunt sapien vulputate ut. Sed laoreet justo odio, id dapibus lacus maximus viverra. Aenean aliquam
      |diam non molestie aliquam. Sed efficitur vehicula erat at laoreet.
      |
      |Nunc tincidunt nulla et fringilla tempor. Nulla consequat, felis vel hendrerit tincidunt, libero quam dictum
      |nibh, ut bibendum purus elit at dui. Ut iaculis posuere leo, in tempus massa blandit at. Quisque nec mauris ut
      |lectus lacinia dignissim. Nunc finibus, purus non pulvinar lacinia, lectus ante placerat eros, nec hendrerit dui
      |augue varius sem. Nam fringilla ligula non fringilla faucibus. Etiam vestibulum viverra neque, id iaculis nisi
      |aliquam quis. Aliquam feugiat tempus risus. Suspendisse non tellus condimentum, molestie sapien vitae, consequat
      |turpis. Etiam efficitur, odio non blandit scelerisque, leo est aliquet turpis, a condimentum lectus quam vel
      |ipsum. Nam fringilla eros vitae sodales laoreet.
      |""".stripMargin).check(ok)))
  private val setAllMessagesInMailboxASeenFlag = imap("storeAll").store(MessageRanges(From(1L)), StoreFlags.add(Silent.Enable(), "\\Seen")).check(ok)
  private val copyAllMessagesToMailboxB = imap("copy").copyMessage(MessageRanges(From(1L)), mailboxBName).check(ok)
  private val moveAllMessagesToMailboxC = imap("move").moveMessage(MessageRanges(From(1L)), mailboxCName).check(debug)
  private val setAllMessagesInMailboxBDeletedFlagAndExpunge = exec(imap("storeAll").store(MessageRanges(From(1L)), StoreFlags.add(Silent.Enable(), "\\Deleted")).check(ok))
    .exec(imap("expunge").expunge().check(ok))
  private val deleteMailboxA_B_C = exec(imap("deleteFolder").deleteFolder(mailboxAName).check(ok))
    .exec(imap("deleteFolder").deleteFolder(mailboxBName).check(ok))
    .exec(imap("deleteFolder").deleteFolder(mailboxCName).check(ok))
  private val createMailboxDWith1000SubMailboxes = exec(imap("createFolder").createFolder(mailboxDName).check(ok))
    .exec(repeat(numberOfSubMailboxes, "loopId")(pause(gracePeriod).exec(exec(imap("createFolder").createFolder(s"$mailboxDName.$${loopId}").check(ok)))))
  private val renameMailboxD = exec(imap("renameFolder").renameFolder(mailboxDName, mailboxDNewName).check(ok))
  private val deleteMailboxD = exec(imap("deleteFolder").deleteFolder(mailboxDNewName).check(ok))
    .exec(repeat(numberOfSubMailboxes, "loopId")(pause(gracePeriod).exec(exec(imap("deleteFolder").deleteFolder(s"$mailboxDNewName.$${loopId}").check(ok)))))


  def apply(feeder: FeederBuilder): ScenarioBuilder =
    scenario("MassiveOperationSupport")
      .feed(feeder)
      .pause(1 second)
      .exec(imap("Connect").connect()).exitHereIfFailed
      .exec(imap("login").login("${username}", "${password}").check(ok))
      .exec(createMailboxA_B_C)
      .exec(imap("select").select(mailboxAName).check(ok))
      .exec(populateMailboxA)
      .pause(2 second)
      .exec(setAllMessagesInMailboxASeenFlag)
      .pause(2 second)
      .exec(copyAllMessagesToMailboxB)
      .pause(2 second)
//      .exec(moveAllMessagesToMailboxC)
//      .pause(2 seconds)
      .exec(imap("select").select(mailboxBName).check(ok))
      .exec(setAllMessagesInMailboxBDeletedFlagAndExpunge)
      .pause(2 seconds)
      .exec(deleteMailboxA_B_C)
      .exec(createMailboxDWith1000SubMailboxes)
      .pause(1 seconds)
      .exec(renameMailboxD)
      .pause(5 seconds)
      .exec(deleteMailboxD)

}
