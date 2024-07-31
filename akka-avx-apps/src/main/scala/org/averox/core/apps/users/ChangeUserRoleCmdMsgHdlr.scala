package org.averox.core.apps.users

import org.averox.common2.msgs._
import org.averox.core.models.{ RegisteredUsers, Roles, UserState, Users2x }
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.LockSettingsUtil
import org.averox.core.db.NotificationDAO
import org.averox.core2.message.senders.{ MsgBuilder, Sender }

trait ChangeUserRoleCmdMsgHdlr extends RightsManagementTrait {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleChangeUserRoleCmdMsg(msg: ChangeUserRoleCmdMsg) {
    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId) || liveMeeting.props.meetingProp.isBreakout) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to change user role in meeting."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
    } else {
      for {
        uvo <- Users2x.findWithIntId(liveMeeting.users2x, msg.body.userId)
      } yield {

        val userRole = if (msg.body.role == Roles.MODERATOR_ROLE) Roles.MODERATOR_ROLE else Roles.VIEWER_ROLE
        for {
          // Update guest from waiting list
          u <- RegisteredUsers.findWithUserId(uvo.intId, liveMeeting.registeredUsers)
        } yield {
          RegisteredUsers.updateUserRole(liveMeeting.registeredUsers, u, userRole)
        }
        val promoteGuest = !liveMeeting.props.usersProp.authenticatedGuest || liveMeeting.props.usersProp.allowPromoteGuestToModerator
        if (msg.body.role == Roles.MODERATOR_ROLE && (!uvo.guest || promoteGuest)) {
          // Promote non-guest users.
          val notifyEvent = MsgBuilder.buildNotifyUserInMeetingEvtMsg(
            msg.body.userId,
            liveMeeting.props.meetingProp.intId,
            "info",
            "user",
            "app.toast.promotedLabel",
            "Notification message when promoted",
            Vector()
          )
          outGW.send(notifyEvent)
          NotificationDAO.insert(notifyEvent)

          Users2x.changeRole(liveMeeting.users2x, uvo, msg.body.role)
          val event = buildUserRoleChangedEvtMsg(liveMeeting.props.meetingProp.intId, msg.body.userId,
            msg.body.changedBy, Roles.MODERATOR_ROLE)
          outGW.send(event)
        } else if (msg.body.role == Roles.VIEWER_ROLE) {
          val notifyEvent = MsgBuilder.buildNotifyUserInMeetingEvtMsg(
            msg.body.userId,
            liveMeeting.props.meetingProp.intId,
            "info",
            "user",
            "app.toast.demotedLabel",
            "Notification message when demoted",
            Vector()
          )
          outGW.send(notifyEvent)
          NotificationDAO.insert(notifyEvent)

          val newUvo: UserState = Users2x.changeRole(liveMeeting.users2x, uvo, msg.body.role)
          val event = buildUserRoleChangedEvtMsg(liveMeeting.props.meetingProp.intId, msg.body.userId,
            msg.body.changedBy, Roles.VIEWER_ROLE)

          LockSettingsUtil.enforceCamLockSettingsForAllUsers(liveMeeting, outGW)

          outGW.send(event)
        }

        // Force reconnection with graphql to refresh permissions
        for {
          u <- RegisteredUsers.findWithUserId(uvo.intId, liveMeeting.registeredUsers)
        } yield {
          Sender.sendForceUserGraphqlReconnectionSysMsg(liveMeeting.props.meetingProp.intId, uvo.intId, u.sessionToken, "role_changed", outGW)
        }
      }
    }
  }

  def buildUserRoleChangedEvtMsg(meetingId: String, userId: String, changedBy: String, role: String): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, meetingId, userId)
    val envelope = BbbCoreEnvelope(UserRoleChangedEvtMsg.NAME, routing)
    val header = BbbClientMsgHeader(UserRoleChangedEvtMsg.NAME, meetingId, changedBy)
    val body = UserRoleChangedEvtMsgBody(userId, role, changedBy)
    val event = UserRoleChangedEvtMsg(header, body)
    BbbCommonEnvCoreMsg(envelope, event)
  }
}