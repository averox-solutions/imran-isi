package org.averox.core.apps.breakout

import org.averox.common2.msgs._
import org.averox.core.api.EndBreakoutRoomInternalMsg
import org.averox.core.bus.AveroxEvent
import org.averox.core.domain.{ MeetingEndReason, MeetingState2x }
import org.averox.core.running.{ MeetingActor, OutMsgRouter }
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.db.{ BreakoutRoomDAO, NotificationDAO, UserBreakoutRoomDAO }
import org.averox.core2.message.senders.MsgBuilder

trait EndAllBreakoutRoomsMsgHdlr extends RightsManagementTrait {
  this: MeetingActor =>

  val outGW: OutMsgRouter

  def handleEndAllBreakoutRoomsMsg(msg: EndAllBreakoutRoomsMsg, state: MeetingState2x): MeetingState2x = {
    val meetingId = liveMeeting.props.meetingProp.intId
    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val reason = "No permission to end breakout rooms for meeting."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
      state
    } else {
      endAllBreakoutRooms(eventBus, liveMeeting, state, MeetingEndReason.BREAKOUT_ENDED_BY_MOD)

      val notifyEvent = MsgBuilder.buildNotifyAllInMeetingEvtMsg(
        meetingId,
        "info",
        "rooms",
        "app.toast.breakoutRoomEnded",
        "Message when the breakout room is ended",
        Vector()
      )
      outGW.send(notifyEvent)
      NotificationDAO.insert(notifyEvent)

      BreakoutRoomDAO.updateRoomsEnded(meetingId)
      state.update(None)
    }
  }
}
