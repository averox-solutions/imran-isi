package org.averox.core.apps.users

import org.averox.common2.msgs._
import org.averox.core.api.SendRecordingTimerInternalMsg
import org.averox.core.domain.MeetingState2x
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core.util.TimeUtil
import org.averox.core2.MeetingStatus2x

trait SendRecordingTimerInternalMsgHdlr {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleSendRecordingTimerInternalMsg(msg: SendRecordingTimerInternalMsg, state: MeetingState2x): MeetingState2x = {
    def buildUpdateRecordingTimerEvtMsg(meetingId: String, recordingTime: Long): BbbCommonEnvCoreMsg = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, meetingId, "not-used")
      val envelope = BbbCoreEnvelope(UpdateRecordingTimerEvtMsg.NAME, routing)
      val body = UpdateRecordingTimerEvtMsgBody(recordingTime)
      val header = BbbClientMsgHeader(UpdateRecordingTimerEvtMsg.NAME, meetingId, "not-used")
      val event = UpdateRecordingTimerEvtMsg(header, body)

      BbbCommonEnvCoreMsg(envelope, event)
    }

    var newDuration = 0L
    if (MeetingStatus2x.isRecording(liveMeeting.status)) {
      newDuration = TimeUtil.timeNowInMs()
      val tracker = state.recordingTracker.udpateCurrentDuration(newDuration)

      val recordingTime = TimeUtil.millisToSeconds(tracker.recordingDuration())

      val event = buildUpdateRecordingTimerEvtMsg(liveMeeting.props.meetingProp.intId, recordingTime)
      outGW.send(event)

      state.update(tracker)
    } else {
      state
    }

  }
}