package org.averox.core.apps.whiteboard

import org.averox.core.running.LiveMeeting
import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.db.PresPageCursorDAO

trait SendCursorPositionPubMsgHdlr extends RightsManagementTrait {
  this: WhiteboardApp2x =>

  def handle(msg: SendCursorPositionPubMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastEvent(msg: SendCursorPositionPubMsg): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(SendCursorPositionEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(SendCursorPositionEvtMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = SendCursorPositionEvtMsgBody(msg.body.whiteboardId, msg.body.xPercent, msg.body.yPercent)
      val event = SendCursorPositionEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    if (filterWhiteboardMessage(msg.body.whiteboardId, msg.header.userId, liveMeeting) && permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to send your cursor position."
      // Just drop messages as these might be delayed messages from multi-user whiteboard. Don't want to
      // eject user unnecessarily when switching from multi-user to single user. (ralam feb 7, 2018)
      //PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW, liveMeeting)
    } else {
      broadcastEvent(msg)
      PresPageCursorDAO.insertOrUpdate(msg.body.whiteboardId, liveMeeting.props.meetingProp.intId, msg.header.userId, msg.body.xPercent, msg.body.yPercent)
    }
  }
}