package org.averox.core.apps.breakout

import org.averox.common2.msgs._
import org.averox.core.domain.MeetingState2x
import org.averox.core.running.{ MeetingActor, OutMsgRouter }
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.db.BreakoutRoomUserDAO
import org.averox.core.models.{ Roles, Users2x }

trait RequestBreakoutJoinURLReqMsgHdlr extends RightsManagementTrait {
  this: MeetingActor =>

  val outGW: OutMsgRouter

  def handleRequestBreakoutJoinURLReqMsg(msg: RequestBreakoutJoinURLReqMsg, state: MeetingState2x): MeetingState2x = {
    if (permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to request breakout room URL for meeting."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
    } else {
      for {
        model <- state.breakout
        room <- model.find(msg.body.breakoutId)
        requesterUser <- Users2x.findWithIntId(liveMeeting.users2x, msg.header.userId)
      } yield {
        if (requesterUser.role == Roles.MODERATOR_ROLE || room.freeJoin) {

          BreakoutRoomUserDAO.insertBreakoutRoom(requesterUser.intId, room, liveMeeting)

          BreakoutHdlrHelpers.sendJoinURL(
            liveMeeting,
            outGW,
            msg.body.userId,
            room.externalId,
            room.sequence.toString(),
            room.id
          )
        } else {
          val meetingId = liveMeeting.props.meetingProp.intId
          val reason = "No permission to request breakout room URL for meeting."
          PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
        }
      }
    }

    state
  }
}
