package org.averox.core2.message.handlers.guests

import org.averox.common2.msgs.GetGuestsWaitingApprovalReqMsg
import org.averox.core.models.GuestsWaiting
import org.averox.core.running.{ BaseMeetingActor, HandlerHelpers, LiveMeeting, OutMsgRouter }
import org.averox.core2.message.senders.{ MsgBuilder }

trait GetGuestsWaitingApprovalReqMsgHdlr extends HandlerHelpers {
  this: BaseMeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleGetGuestsWaitingApprovalReqMsg(msg: GetGuestsWaitingApprovalReqMsg): Unit = {
    val guests = GuestsWaiting.findAll(liveMeeting.guestsWaiting)
    val event = MsgBuilder.buildGetGuestsWaitingApprovalRespMsg(
      liveMeeting.props.meetingProp.intId,
      msg.body.requesterId,
      guests
    )

    outGW.send(event)

  }

}
