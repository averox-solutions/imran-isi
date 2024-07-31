package org.averox.core.apps.timer

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.running.LiveMeeting
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait, TimerModel }
import org.averox.core.db.TimerDAO

trait StopTimerReqMsgHdlr extends RightsManagementTrait {
  this: TimerApp2x =>

  def handle(msg: StopTimerReqMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    log.debug("Received stopTimerReqMsg {}", StopTimerReqMsg)
    def broadcastEvent(
        accumulated: Int
    ): Unit = {
      val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
      val envelope = BbbCoreEnvelope(StopTimerRespMsg.NAME, routing)
      val header = BbbCoreHeaderWithMeetingId(
        StopTimerRespMsg.NAME,
        liveMeeting.props.meetingProp.intId
      )
      val body = StopTimerRespMsgBody(msg.header.userId, accumulated)
      val event = StopTimerRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId) &&
      permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL, liveMeeting.users2x, msg.header.userId) &&
      msg.header.userId != "nodeJSapp") {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "You need to be the presenter or moderator to stop timer"
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW, liveMeeting)
    } else {
      TimerModel.setRunning(liveMeeting.timerModel, running = false)
      TimerDAO.update(liveMeeting.props.meetingProp.intId, liveMeeting.timerModel)
      broadcastEvent(TimerModel.getAccumulated(liveMeeting.timerModel))
    }
  }
}
