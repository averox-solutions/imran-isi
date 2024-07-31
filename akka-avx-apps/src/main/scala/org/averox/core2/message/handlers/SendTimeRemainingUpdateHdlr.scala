package org.averox.core2.message.handlers

import org.averox.core.api.SendTimeRemainingAuditInternalMsg
import org.averox.core.domain.MeetingState2x
import org.averox.core.running.{ BaseMeetingActor, LiveMeeting, OutMsgRouter }
import org.averox.core.util.TimeUtil
import org.averox.core2.message.senders.MsgBuilder

trait SendTimeRemainingUpdateHdlr {
  this: BaseMeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleSendTimeRemainingUpdate(msg: SendTimeRemainingAuditInternalMsg, state: MeetingState2x): MeetingState2x = {
    if (state.expiryTracker.durationInMs > 0) {
      val endMeetingTime = state.expiryTracker.endMeetingTime()
      val timeRemaining = TimeUtil.millisToSeconds(endMeetingTime - TimeUtil.timeNowInMs())

      if (timeRemaining > 0) {
        val event = MsgBuilder.buildMeetingTimeRemainingUpdateEvtMsg(liveMeeting.props.meetingProp.intId, timeRemaining.toInt)
        outGW.send(event)
      }
    }

    state
  }

}
