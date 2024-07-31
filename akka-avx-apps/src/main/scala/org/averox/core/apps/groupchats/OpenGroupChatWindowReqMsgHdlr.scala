package org.averox.core.apps.groupchats

import org.averox.common2.msgs.{ OpenGroupChatWindowReqMsg }
import org.averox.core.bus.MessageBus
import org.averox.core.domain.MeetingState2x
import org.averox.core.running.LiveMeeting

trait OpenGroupChatWindowReqMsgHdlr {
  this: GroupChatHdlrs =>

  def handle(msg: OpenGroupChatWindowReqMsg, state: MeetingState2x,
             liveMeeting: LiveMeeting, bus: MessageBus): MeetingState2x = {
    state
  }
}
