package org.averox.core.apps.breakout

import org.averox.common2.msgs._
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.db.BreakoutRoomUserDAO
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.{ Roles, Users2x }
import org.averox.core.running.{ MeetingActor, OutMsgRouter }

trait SetBreakoutRoomInviteDismissedReqMsgHdlr extends RightsManagementTrait {
  this: MeetingActor =>

  val outGW: OutMsgRouter

  def handleSetBreakoutRoomInviteDismissedReqMsg(msg: SetBreakoutRoomInviteDismissedReqMsg) = {
    for {
      requesterUser <- Users2x.findWithIntId(liveMeeting.users2x, msg.header.userId)
    } yield {
      BreakoutRoomUserDAO.updateInviteDismissedAt(requesterUser.meetingId, requesterUser.intId)
    }
  }
}
