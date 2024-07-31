package org.averox.core.apps.voice

import org.averox.SystemConfiguration
import org.averox.common2.msgs.VoiceConfRunningEvtMsg
import org.averox.core.running.{ BaseMeetingActor, LiveMeeting, OutMsgRouter }
import org.averox.core2.MeetingStatus2x
import org.averox.core.apps.voice.VoiceApp

trait VoiceConfRunningEvtMsgHdlr extends SystemConfiguration {
  this: BaseMeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleVoiceConfRunningEvtMsg(msg: VoiceConfRunningEvtMsg): Unit = {
    log.info("Received VoiceConfRunningEvtMsg " + msg.body.running)

    if (liveMeeting.props.recordProp.record) {
      if (msg.body.running &&
        (MeetingStatus2x.isRecording(liveMeeting.status) || liveMeeting.props.recordProp.recordFullDurationMedia)) {
        val meetingId = liveMeeting.props.meetingProp.intId
        log.info("Send START RECORDING voice conf. meetingId=" + meetingId + " voice conf=" + liveMeeting.props.voiceProp.voiceConf)

        VoiceApp.startRecordingVoiceConference(liveMeeting, outGW)
      } else {
        VoiceApp.stopRecordingVoiceConference(liveMeeting, outGW)
      }
    }
  }
}
