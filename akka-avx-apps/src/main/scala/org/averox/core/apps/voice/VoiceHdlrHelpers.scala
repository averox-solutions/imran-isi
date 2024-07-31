package org.averox.core.apps.voice

import org.averox.common2.msgs._
import org.averox.core.models.{ Users2x, VoiceUsers }
import org.averox.core.running.{ LiveMeeting }
import org.averox.LockSettingsUtil
import org.averox.SystemConfiguration

object VoiceHdlrHelpers extends SystemConfiguration {
  def isGlobalAudioSubscribeAllowed(
      liveMeeting: LiveMeeting,
      meetingId:   String,
      userId:      String,
      voiceConf:   String
  ): Boolean = {
    Users2x.findWithIntId(liveMeeting.users2x, userId) match {
      case Some(user) => (
        applyPermissionCheck &&
        liveMeeting.props.meetingProp.intId == meetingId &&
        liveMeeting.props.voiceProp.voiceConf == voiceConf
      )
      case _ => false
    }
  }

  def isMicrophoneSharingAllowed(
      liveMeeting: LiveMeeting,
      meetingId:   String,
      userId:      String,
      voiceConf:   String,
      callerIdNum: String
  ): Boolean = {
    Users2x.findWithIntId(liveMeeting.users2x, userId) match {
      case Some(user) => {
        val microphoneSharingLocked = LockSettingsUtil.isMicrophoneSharingLocked(
          user,
          liveMeeting
        )
        val isCallerBanned = VoiceUsers.isCallerBanned(
          callerIdNum,
          liveMeeting.voiceUsers
        )

        (applyPermissionCheck &&
          !isCallerBanned &&
          !microphoneSharingLocked &&
          liveMeeting.props.meetingProp.intId == meetingId &&
          liveMeeting.props.voiceProp.voiceConf == voiceConf)
      }
      case _ => false
    }
  }
}
