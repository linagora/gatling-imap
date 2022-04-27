package com.linagora.gatling.imap

import java.util.Calendar

import com.linagora.gatling.imap.protocol.command.{FetchAttributes, MessageRanges, StoreFlags}
import javax.mail.search.SearchTerm

import scala.collection.immutable.Seq

package object protocol {

  case class Domain(value: String) extends AnyVal
  case class User(login: String, password: String)

  case class StatusItems(items: Seq[StatusItem])

  // 33 STATUS "test" (UIDNEXT MESSAGES UNSEEN RECENT)
  sealed trait StatusItem {
    def asString: String
  }
  case object UidNext extends StatusItem {
    override def asString: String = "UIDNEXT"
  }
  case object Messages extends StatusItem {
    override def asString: String = "MESSAGES"
  }
  case object Unseen extends StatusItem {
    override def asString: String = "UNSEEN"
  }
  case object Recent extends StatusItem {
    override def asString: String = "RECENT"
  }

  trait Command {
    def userId: UserId
  }

  object Command {

    case class Connect(userId: UserId) extends Command

    case class Idle(userId: UserId) extends Command

    case class Close(userId: UserId) extends Command

    case class Logout(userId: UserId) extends Command

    case class Capability(userId: UserId) extends Command

    case class Unselect(userId: UserId) extends Command

    case class Noop(userId: UserId) extends Command

    case class Namespace(userId: UserId) extends Command

    case class Subscribe(userId: UserId, mailbox: String) extends Command

    case class Unsubscribe(userId: UserId, mailbox: String) extends Command

    case class CreateFolder(userId: UserId, mailbox: String) extends Command

    case class DeleteFolder(userId: UserId, mailbox: String) extends Command

    case class RenameFolder(userId: UserId, oldFolder: String, newFolder: String) extends Command

    case class ExamineFolder(userId: UserId, mailbox: String) extends Command

    case class Check(userId: UserId) extends Command

    case class Enable(userId: UserId, capability: String) extends Command

    case class Login(userId: UserId, username: String, password: String) extends Command

    object Login {
      def apply(userId: UserId, user: User): Login = new Login(userId, user.login, user.password)
    }

    case class Select(userId: UserId, mailbox: String) extends Command

    case class GetQuotaRoot(userId: UserId, mailbox: String) extends Command

    case class GetQuota(userId: UserId, quotaRoot: String) extends Command

    case class SetQuota(userId: UserId, quotaRootAndResourcesLimits: String) extends Command

    case class GetAcl(userId: UserId, mailbox: String) extends Command

    case class MyRights(userId: UserId, mailbox: String) extends Command

    case class List(userId: UserId, reference: String, mailbox: String) extends Command

    case class Lsub(userId: UserId, reference: String, mailbox: String) extends Command

    case class Fetch(userId: UserId, sequence: MessageRanges, attributes: FetchAttributes) extends Command

    case class UIDFetch(userId: UserId, sequence: MessageRanges, attributes: FetchAttributes) extends Command

    case class Search(userId: UserId, sequence: MessageRanges, searchTerm: SearchTerm) extends Command

    case class UIDSearch(userId: UserId, sequence: MessageRanges, searchTerm: SearchTerm) extends Command

    case class Move(userId: UserId, sequence: MessageRanges, mailbox: String) extends Command

    case class Copy(userId: UserId, sequence: MessageRanges, mailbox: String) extends Command

    case class UidCopy(userId: UserId, sequence: MessageRanges, mailbox: String) extends Command

    case class UidMove(userId: UserId, sequence: MessageRanges, mailbox: String) extends Command

    case class UidExpunge(userId: UserId, sequence: MessageRanges) extends Command

    case class Expunge(userId: UserId) extends Command

    case class Append(userId: UserId, mailbox: String, flags: Option[Seq[String]], date: Option[Calendar], content: String) extends Command

    case class Disconnect(userId: UserId) extends Command

    case class Status(userId: UserId, mailbox: String, items: StatusItems) extends Command

    case class Store(userId: UserId, sequence: MessageRanges, flags: StoreFlags) extends Command
  }

  sealed trait Response {
    def responses: ImapResponses
  }

  object Response {
    def unapply(arg: Response): Option[ImapResponses] = Some(arg.responses)

    case class Connected(responses: ImapResponses) extends Response

    case class LoggedIn(responses: ImapResponses) extends Response

    case class SearchResult(responses: ImapResponses) extends Response

    case class UIDSearchResult(responses: ImapResponses) extends Response

    case class MoveResult(responses: ImapResponses) extends Response

    case class CopyResult(responses: ImapResponses) extends Response

    case class UidCopyResult(responses: ImapResponses) extends Response

    case class UidMoveResult(responses: ImapResponses) extends Response

    case class UidExpungeResult(responses: ImapResponses) extends Response

    case class Closed(responses: ImapResponses) extends Response

    case class LoggedOut(responses: ImapResponses) extends Response

    case class Nooped(responses: ImapResponses) extends Response

    case class Idled(responses: ImapResponses) extends Response

    case class Capabilities(responses: ImapResponses) extends Response

    case class UnSelected(responses: ImapResponses) extends Response

    case class Checked(responses: ImapResponses) extends Response

    case class Enabled(responses: ImapResponses) extends Response

    case class Selected(responses: ImapResponses) extends Response

    case class Status(responses: ImapResponses) extends Response

    case class QuotaRootResponse(responses: ImapResponses) extends Response

    case class QuotaResponse(responses: ImapResponses) extends Response

    case class SetQuotaResponse(responses: ImapResponses) extends Response

    case class AclResponse(responses: ImapResponses) extends Response

    case class MyRightsResponse(responses: ImapResponses) extends Response

    case class NamespaceResponse(responses: ImapResponses) extends Response

    case class SubscribeResponse(responses: ImapResponses) extends Response

    case class UnsubscribeResponse(responses: ImapResponses) extends Response

    case class CreateFolderResponse(responses: ImapResponses) extends Response

    case class DeleteFolderResponse(responses: ImapResponses) extends Response

    case class RenameFolderResponse(responses: ImapResponses) extends Response

    case class ExamineFolderResponse(responses: ImapResponses) extends Response

    case class Listed(responses: ImapResponses) extends Response

    case class Lsubed(responses: ImapResponses) extends Response

    case class Fetched(responses: ImapResponses) extends Response

    case class Expunged(responses: ImapResponses) extends Response

    case class Stored(responses: ImapResponses) extends Response

    case class Appended(responses: ImapResponses) extends Response

    case class Disconnected(message: String)

  }

}

