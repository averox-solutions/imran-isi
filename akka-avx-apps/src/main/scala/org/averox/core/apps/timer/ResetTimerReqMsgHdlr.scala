package org.averox.core.apps.timer

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.running.LiveMeeting
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait, TimerModel }
import org.averox.core.db.TimerDAO

trait ResetTimerReqMsgHdlr extends RightsManagementTrait {
  this: TimerApp2x =>

  def handle(msg: ResetTimerReqMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    log.debug("Received resetTimerReqMsg {}", ResetTimerReqMsg)
    def broadcastEvent(): Unit = {
      val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
      val envelope = BbbCoreEnvelope(ResetTimerRespMsg.NAME, routing)
      val header = BbbCoreHeaderWithMeetingId(
        ResetTimerRespMsg.NAME,
        liveMeeting.props.meetingProp.intId
      )
      val body = ResetTimerRespMsgBody(msg.header.userId)
      val event = ResetTimerRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId) &&
      permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "You need to be the presenter or moderator to reset timer"
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW, liveMeeting)
    } else {
      TimerModel.reset(liveMeeting.timerModel)
      TimerDAO.update(liveMeeting.props.meetingProp.intId, liveMeeting.timerModel)
      broadcastEvent()
    }
  }
}
