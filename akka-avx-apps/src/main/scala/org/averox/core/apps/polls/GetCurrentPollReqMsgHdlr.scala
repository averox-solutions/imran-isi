package org.averox.core.apps.polls

import org.averox.common2.domain.PollVO
import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.Polls
import org.averox.core.running.LiveMeeting

trait GetCurrentPollReqMsgHdlr {
  this: PollApp2x =>

  def handle(msgIn: GetCurrentPollReqMsg, state: MeetingState2x, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastEvent(msg: GetCurrentPollReqMsg, hasPoll: Boolean, pvo: Option[PollVO]): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(GetCurrentPollRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(GetCurrentPollRespMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = GetCurrentPollRespMsgBody(msg.header.userId, hasPoll, pvo)
      val event = GetCurrentPollRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    val pollVO = Polls.handleGetCurrentPollReqMsg(state, msgIn.header.userId, liveMeeting)

    pollVO match {
      case Some(poll) =>
        broadcastEvent(msgIn, true, pollVO)
      case None =>
        broadcastEvent(msgIn, false, None)
    }
  }
}
