package org.averox.core.apps.whiteboard

import org.averox.core.running.LiveMeeting
import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }

trait ClearWhiteboardPubMsgHdlr extends RightsManagementTrait {
  this: WhiteboardApp2x =>

  def handle(msg: ClearWhiteboardPubMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastEvent(msg: ClearWhiteboardPubMsg, fullClear: Boolean): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(ClearWhiteboardEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(ClearWhiteboardEvtMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = ClearWhiteboardEvtMsgBody(msg.body.whiteboardId, msg.header.userId, fullClear)
      val event = ClearWhiteboardEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    if (filterWhiteboardMessage(msg.body.whiteboardId, msg.header.userId, liveMeeting) && permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      if (isNonEjectionGracePeriodOver(msg.body.whiteboardId, msg.header.userId, liveMeeting)) {
        val meetingId = liveMeeting.props.meetingProp.intId
        val reason = "No permission to clear the whiteboard."
        PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW, liveMeeting)
      }
    } else {
      log.error("Ignoring message ClearWhiteboardPubMsg since this functions is not available in the new Whiteboard")
    }
  }
}