package org.averox.core.apps.breakout

import org.averox.core.api.SendBreakoutTimeRemainingInternalMsg
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.message.senders.MsgBuilder

trait SendBreakoutTimeRemainingInternalMsgHdlr {
  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleSendBreakoutTimeRemainingInternalMsg(msg: SendBreakoutTimeRemainingInternalMsg): Unit = {
    val event = MsgBuilder.buildMeetingTimeRemainingUpdateEvtMsg(liveMeeting.props.meetingProp.intId, msg.timeLeftInSec.toInt, msg.timeUpdatedInMinutes)
    outGW.send(event)
  }
}
