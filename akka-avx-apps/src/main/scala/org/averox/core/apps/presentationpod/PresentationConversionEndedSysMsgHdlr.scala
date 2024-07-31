package org.averox.core.apps.presentationpod

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.domain.MeetingState2x
import org.averox.core.running.LiveMeeting

trait PresentationConversionEndedSysMsgHdlr {
  this: PresentationPodHdlrs =>

  def handle(msg: PresentationConversionEndedSysMsg, state: MeetingState2x,
             liveMeeting: LiveMeeting, bus: MessageBus): MeetingState2x = {

    def broadcastEvent(msg: PresentationConversionEndedSysMsg): Unit = {
      val routing = Routing.addMsgToClientRouting(
        MessageTypes.BROADCAST_TO_MEETING,
        liveMeeting.props.meetingProp.intId, msg.header.userId
      )
      val envelope = BbbCoreEnvelope(PresentationConversionEndedEventMsg.NAME, routing)
      val header = BbbClientMsgHeader(
        PresentationConversionEndedEventMsg.NAME,
        liveMeeting.props.meetingProp.intId, msg.header.userId
      )

      val body = PresentationConversionEndedEventMsgBody(
        podId = msg.body.podId,
        presentationId = msg.body.presentationId,
        presName = msg.body.presName
      )
      val event = PresentationConversionEndedEventMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    broadcastEvent(msg)

    state
  }

}
