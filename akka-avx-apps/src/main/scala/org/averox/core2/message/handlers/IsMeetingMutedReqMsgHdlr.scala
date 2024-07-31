package org.averox.core2.message.handlers

import org.averox.common2.msgs._
import org.averox.core.running.{ MeetingActor, OutMsgRouter }
import org.averox.core2.MeetingStatus2x

trait IsMeetingMutedReqMsgHdlr {
  this: MeetingActor =>

  val outGW: OutMsgRouter

  def handleIsMeetingMutedReqMsg(msg: IsMeetingMutedReqMsg) {

    def build(meetingId: String, userId: String, muted: Boolean): BbbCommonEnvCoreMsg = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
      val envelope = BbbCoreEnvelope(IsMeetingMutedRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(IsMeetingMutedRespMsg.NAME, meetingId, userId)

      val body = IsMeetingMutedRespMsgBody(muted)
      val event = IsMeetingMutedRespMsg(header, body)

      BbbCommonEnvCoreMsg(envelope, event)
    }

    val event = build(liveMeeting.props.meetingProp.intId, msg.header.userId, MeetingStatus2x.isMeetingMuted(liveMeeting.status))
    outGW.send(event)
  }
}
