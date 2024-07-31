package org.averox.core.apps.users

import org.averox.common2.msgs._
import org.averox.core.bus.InternalEventBus
import org.averox.core.domain.{ MeetingEndReason, MeetingState2x }
import org.averox.core.models.{ Roles, Users2x }
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }

trait LogoutAndEndMeetingCmdMsgHdlr extends RightsManagementTrait {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter
  val eventBus: InternalEventBus

  def handleLogoutAndEndMeetingCmdMsg(msg: LogoutAndEndMeetingCmdMsg, state: MeetingState2x): Unit = {
    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId) || liveMeeting.props.meetingProp.isBreakout) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to end meeting on logout."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
    } else {
      for {
        u <- Users2x.findWithIntId(liveMeeting.users2x, msg.body.userId)
      } yield {
        if (u.role == Roles.MODERATOR_ROLE) {
          endAllBreakoutRooms(eventBus, liveMeeting, state, MeetingEndReason.ENDED_AFTER_USER_LOGGED_OUT)
          log.info("Meeting {} ended by user [{}, {}} when logging out.", liveMeeting.props.meetingProp.intId,
            u.intId, u.name)
          sendEndMeetingDueToExpiry(MeetingEndReason.ENDED_AFTER_USER_LOGGED_OUT, eventBus, outGW, liveMeeting, u.intId)
        }
      }
    }
  }
}
