package org.averox.core.apps.audiocaptions

import org.averox.common2.msgs._
import org.averox.core.models.{ AudioCaptions, VoiceUsers }
import org.averox.core.running.LiveMeeting

trait AudioFloorChangedVoiceConfEvtMsgHdlr {
  this: AudioCaptionsApp2x =>

  def handle(msg: AudioFloorChangedVoiceConfEvtMsg, liveMeeting: LiveMeeting): Unit = {
    for {
      vu <- VoiceUsers.findWithVoiceUserId(liveMeeting.voiceUsers, msg.body.voiceUserId)
    } yield AudioCaptions.setFloor(liveMeeting.audioCaptions, vu.intId)
  }
}
