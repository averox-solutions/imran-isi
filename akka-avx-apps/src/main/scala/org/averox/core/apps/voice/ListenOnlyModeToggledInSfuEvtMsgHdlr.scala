package org.averox.core.apps.voice

import org.averox.common2.msgs._
import org.averox.core.models.VoiceUsers
import org.averox.core.running.{ BaseMeetingActor, LiveMeeting, OutMsgRouter }

trait ListenOnlyModeToggledInSfuEvtMsgHdlr {
  this: BaseMeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleListenOnlyModeToggledInSfuEvtMsg(msg: ListenOnlyModeToggledInSfuEvtMsg): Unit = {
    for {
      vu <- VoiceUsers.findWithIntId(liveMeeting.voiceUsers, msg.body.userId)
    } yield {
      VoiceApp.holdChannelInVoiceConf(
        liveMeeting,
        outGW,
        vu.uuid,
        msg.body.enabled
      )
    }
  }
}
