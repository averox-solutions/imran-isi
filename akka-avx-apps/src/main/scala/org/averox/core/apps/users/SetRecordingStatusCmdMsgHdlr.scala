package org.averox.core.apps.users

import org.averox.common2.msgs._
import org.averox.core.domain.MeetingState2x
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.MeetingStatus2x
import org.averox.core.util.TimeUtil
import org.averox.core.bus.AveroxEvent
import org.averox.core.api.SendRecordingTimerInternalMsg
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core2.message.senders.MsgBuilder
import org.averox.core.apps.voice.VoiceApp
import org.averox.core.db.{ MeetingRecordingDAO, NotificationDAO }

trait SetRecordingStatusCmdMsgHdlr extends RightsManagementTrait {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleSetRecordingStatusCmdMsg(msg: SetRecordingStatusCmdMsg, state: MeetingState2x): MeetingState2x = {
    log.info("Change recording status. meetingId=" + liveMeeting.props.meetingProp.intId + " recording=" + msg.body.recording)

    def buildRecordingStatusChangedEvtMsg(meetingId: String, userId: String, recording: Boolean): BbbCommonEnvCoreMsg = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, meetingId, userId)
      val envelope = BbbCoreEnvelope(RecordingStatusChangedEvtMsg.NAME, routing)
      val body = RecordingStatusChangedEvtMsgBody(recording, userId)
      val header = BbbClientMsgHeader(RecordingStatusChangedEvtMsg.NAME, meetingId, userId)
      val event = RecordingStatusChangedEvtMsg(header, body)

      BbbCommonEnvCoreMsg(envelope, event)
    }

    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to clear chat in meeting."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
      state
    } else {
      if (liveMeeting.props.recordProp.allowStartStopRecording &&
        MeetingStatus2x.isRecording(liveMeeting.status) != msg.body.recording) {
        if (msg.body.recording) {
          val notifyEvent = MsgBuilder.buildNotifyAllInMeetingEvtMsg(
            liveMeeting.props.meetingProp.intId,
            "success",
            "record",
            "app.notification.recordingStart",
            "Notification for when the recording starts",
            Vector()
          )
          outGW.send(notifyEvent)
          NotificationDAO.insert(notifyEvent)

          MeetingStatus2x.recordingStarted(liveMeeting.status)
          MeetingRecordingDAO.insertRecording(liveMeeting.props.meetingProp.intId, msg.body.setBy)

          // If meeting is not set to record full duration media, then we need to
          // start recording media here. Audio/FS recording is triggered here;
          // SFU intercepts this event and toggles rec for video and screen sharing.
          if (!liveMeeting.props.recordProp.recordFullDurationMedia) {
            log.info("Send START RECORDING voice conf. meetingId=" +
              liveMeeting.props.meetingProp.intId +
              " voice conf=" + liveMeeting.props.voiceProp.voiceConf)
            VoiceApp.startRecordingVoiceConference(
              liveMeeting,
              outGW
            )
          }
        } else {
          val notifyEvent = MsgBuilder.buildNotifyAllInMeetingEvtMsg(
            liveMeeting.props.meetingProp.intId,
            "error",
            "record",
            "app.notification.recordingPaused",
            "Notification for when the recording stops",
            Vector()
          )
          outGW.send(notifyEvent)
          NotificationDAO.insert(notifyEvent)

          MeetingStatus2x.recordingStopped(liveMeeting.status)
          MeetingRecordingDAO.updateStopped(liveMeeting.props.meetingProp.intId, msg.body.setBy)

          // If meeting is not set to record full duration media, then we need to stop recording
          if (!liveMeeting.props.recordProp.recordFullDurationMedia) {
            VoiceApp.stopRecordingVoiceConference(
              liveMeeting,
              outGW
            )
          }
        }

        val event = buildRecordingStatusChangedEvtMsg(liveMeeting.props.meetingProp.intId, msg.body.setBy, msg.body.recording)
        outGW.send(event)

        var newState = state
        if (MeetingStatus2x.isRecording(liveMeeting.status)) {
          val tracker = state.recordingTracker.startTimer(TimeUtil.timeNowInMs())
          newState = state.update(tracker)
        } else {
          val tracker = state.recordingTracker.pauseTimer(TimeUtil.timeNowInMs())
          newState = state.update(tracker)
        }
        eventBus.publish(AveroxEvent(liveMeeting.props.meetingProp.intId, SendRecordingTimerInternalMsg(liveMeeting.props.meetingProp.intId)))
        newState
      } else {
        state
      }
    }

  }
}
