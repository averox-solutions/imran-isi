package org.averox.core.apps.voice

import org.averox.common2.msgs._
import org.averox.core.models.{ VoiceUsers }
import org.averox.core.running.{ LiveMeeting, MeetingActor, OutMsgRouter }

trait UserMutedInVoiceConfEvtMsgHdlr {
  this: MeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleUserMutedInVoiceConfEvtMsg(msg: UserMutedInVoiceConfEvtMsg): Unit = {

    for {
      vu <- VoiceUsers.findWithVoiceUserId(liveMeeting.voiceUsers, msg.body.voiceUserId)
    } yield {
      VoiceApp.handleUserMutedInVoiceConfEvtMsg(
        liveMeeting,
        outGW,
        msg.body.voiceUserId,
        msg.body.muted
      )
    }
  }
}
