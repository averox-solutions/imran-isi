package org.averox.core.apps.timer

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.running.LiveMeeting
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait, TimerModel }
import org.averox.core.db.TimerDAO

trait SetTimerReqMsgHdlr extends RightsManagementTrait {
  this: TimerApp2x =>

  def handle(msg: SetTimerReqMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    log.debug("Received setTimerReqMsg {}", SetTimerReqMsg)
    def broadcastEvent(
        time: Int
    ): Unit = {
      val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
      val envelope = BbbCoreEnvelope(SetTimerRespMsg.NAME, routing)
      val header = BbbCoreHeaderWithMeetingId(
        SetTimerRespMsg.NAME,
        liveMeeting.props.meetingProp.intId
      )
      val body = SetTimerRespMsgBody(msg.header.userId, time)
      val event = SetTimerRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId) &&
      permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "You need to be the presenter or moderator to set timer"
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW, liveMeeting)
    } else {
      TimerModel.setTime(liveMeeting.timerModel, msg.body.time)
      TimerDAO.update(liveMeeting.props.meetingProp.intId, liveMeeting.timerModel)
      broadcastEvent(msg.body.time)
    }
  }
}
