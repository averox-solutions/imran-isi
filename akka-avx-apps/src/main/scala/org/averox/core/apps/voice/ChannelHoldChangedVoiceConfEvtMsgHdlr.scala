package org.averox.core.apps.voice

import org.averox.common2.msgs._
import org.averox.core.running.{ MeetingActor, LiveMeeting, OutMsgRouter }

trait ChannelHoldChangedVoiceConfEvtMsgHdlr {
  this: MeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleChannelHoldChangedVoiceConfEvtMsg(msg: ChannelHoldChangedVoiceConfEvtMsg): Unit = {
    VoiceApp.handleChannelHoldChanged(
      liveMeeting,
      outGW,
      msg.body.intId,
      msg.body.uuid,
      msg.body.hold
    )
  }
}
