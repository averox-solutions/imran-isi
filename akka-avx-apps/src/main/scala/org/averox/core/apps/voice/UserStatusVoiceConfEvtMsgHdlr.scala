package org.averox.core.apps.voice

import org.averox.common2.msgs.UserStatusVoiceConfEvtMsg
import org.averox.core.running.{ LiveMeeting, MeetingActor, OutMsgRouter }

trait UserStatusVoiceConfEvtMsgHdlr {
  this: MeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleUserStatusVoiceConfEvtMsg(msg: UserStatusVoiceConfEvtMsg): Unit = {

    VoiceApp.processUserStatusVoiceConfEvtMsg(
      liveMeeting,
      outGW,
      eventBus,
      msg.body.confUsers
    )
  }
}
