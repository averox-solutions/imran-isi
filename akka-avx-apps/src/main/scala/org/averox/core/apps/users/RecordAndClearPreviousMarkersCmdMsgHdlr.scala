package org.averox.core.apps.users

import org.averox.common2.msgs._
import org.averox.core.db.MeetingRecordingDAO
import org.averox.core.domain.MeetingState2x
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.MeetingStatus2x
import org.averox.core.util.TimeUtil
import org.averox.core2.message.senders.MsgBuilder

trait RecordAndClearPreviousMarkersCmdMsgHdlr {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleRecordAndClearPreviousMarkersCmdMsg(msg: RecordAndClearPreviousMarkersCmdMsg, state: MeetingState2x): MeetingState2x = {
    log.info("Set a new recording marker and clear previous ones. meetingId=" + liveMeeting.props.meetingProp.intId + " recording=" + msg.body.recording)

    def buildRecordingStatusChangedEvtMsg(meetingId: String, userId: String, recording: Boolean): BbbCommonEnvCoreMsg = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, meetingId, userId)
      val envelope = BbbCoreEnvelope(RecordingStatusChangedEvtMsg.NAME, routing)
      val body = RecordingStatusChangedEvtMsgBody(recording, userId)
      val header = BbbClientMsgHeader(RecordingStatusChangedEvtMsg.NAME, meetingId, userId)
      val event = RecordingStatusChangedEvtMsg(header, body)

      BbbCommonEnvCoreMsg(envelope, event)
    }

    // Do not allow stop recording and clear previous markers
    if (liveMeeting.props.recordProp.allowStartStopRecording &&
      MeetingStatus2x.isRecording(liveMeeting.status) != msg.body.recording) {

      MeetingStatus2x.recordingStarted(liveMeeting.status)
      MeetingRecordingDAO.insertRecording(liveMeeting.props.meetingProp.intId, msg.body.setBy)

      val tracker = state.recordingTracker.resetTimer(TimeUtil.timeNowInMs())
      val event = buildRecordingStatusChangedEvtMsg(liveMeeting.props.meetingProp.intId, msg.body.setBy, msg.body.recording)
      outGW.send(event)

      outGW.send(MsgBuilder.buildRecordStatusResetSysMsg(liveMeeting.props.meetingProp.intId, msg.body.recording, msg.body.setBy))

      state.update(tracker)
    } else {
      state
    }
  }
}
