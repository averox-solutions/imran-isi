package org.averox.core.apps.externalvideo

import org.averox.common2.msgs._
import org.averox.core.apps.{ ExternalVideoModel, PermissionCheck, RightsManagementTrait }
import org.averox.core.bus.MessageBus
import org.averox.core.db.ExternalVideoDAO
import org.averox.core.running.LiveMeeting
import org.averox.core2.message.senders.MsgBuilder

trait StopExternalVideoPubMsgHdlr extends RightsManagementTrait {
  this: ExternalVideoApp2x =>

  def handle(msg: StopExternalVideoPubMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    log.info("Received StopExternalVideoPubMsgr meetingId={}", liveMeeting.props.meetingProp.intId)

    if (permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "You need to be the presenter to stop external video"
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW, liveMeeting)
    } else {
      ExternalVideoModel.clear(liveMeeting.externalVideoModel)

      ExternalVideoDAO.updateStoppedSharing(liveMeeting.props.meetingProp.intId)

      //broadcastEvent
      val msgEvent = MsgBuilder.buildStopExternalVideoEvtMsg(liveMeeting.props.meetingProp.intId, msg.header.userId)
      bus.outGW.send(msgEvent)
    }
  }
}
