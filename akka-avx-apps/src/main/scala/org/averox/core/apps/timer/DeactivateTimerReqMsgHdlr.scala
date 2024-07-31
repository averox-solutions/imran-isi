package org.averox.core.apps.timer

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.running.LiveMeeting
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait, TimerModel }
import org.averox.core.db.TimerDAO

trait DeactivateTimerReqMsgHdlr extends RightsManagementTrait {
  this: TimerApp2x =>

  def handle(msg: DeactivateTimerReqMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    log.debug("Received deactivateTimerReqMsg {}", DeactivateTimerReqMsg)
    def broadcastEvent(): Unit = {
      val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
      val envelope = BbbCoreEnvelope(DeactivateTimerRespMsg.NAME, routing)
      val header = BbbCoreHeaderWithMeetingId(
        DeactivateTimerRespMsg.NAME,
        liveMeeting.props.meetingProp.intId
      )
      val body = DeactivateTimerRespMsgBody(msg.header.userId)
      val event = DeactivateTimerRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId) &&
      permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "You need to be the presenter or moderator to deactivate timer"
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW, liveMeeting)
    } else {
      TimerModel.setRunning(liveMeeting.timerModel, running = false)
      TimerModel.setIsActive(liveMeeting.timerModel, active = false)
      TimerModel.setStopwatch(liveMeeting.timerModel, stopwatch = true)
      TimerModel.reset(liveMeeting.timerModel)
      TimerDAO.update(liveMeeting.props.meetingProp.intId, liveMeeting.timerModel)
      broadcastEvent()
    }
  }
}
