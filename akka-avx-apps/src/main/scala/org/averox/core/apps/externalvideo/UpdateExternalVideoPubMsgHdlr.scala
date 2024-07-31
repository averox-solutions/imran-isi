package org.averox.core.apps.externalvideo

import org.averox.common2.msgs._
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.bus.MessageBus
import org.averox.core.db.ExternalVideoDAO
import org.averox.core.running.LiveMeeting

trait UpdateExternalVideoPubMsgHdlr extends RightsManagementTrait {

  def handle(msg: UpdateExternalVideoPubMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    def broadcastEvent(msg: UpdateExternalVideoPubMsg) {
      val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, liveMeeting.props.meetingProp.intId, "nodeJSapp")
      val envelope = BbbCoreEnvelope(UpdateExternalVideoEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(UpdateExternalVideoEvtMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = UpdateExternalVideoEvtMsgBody(msg.body.status, msg.body.rate, msg.body.time, msg.body.state)
      val event = UpdateExternalVideoEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    if (permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "You need to be the presenter to update external video"
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW, liveMeeting)
    } else {
      ExternalVideoDAO.update(liveMeeting.props.meetingProp.intId, msg.body.status, msg.body.rate, msg.body.time, msg.body.state)
      broadcastEvent(msg)
    }
  }
}