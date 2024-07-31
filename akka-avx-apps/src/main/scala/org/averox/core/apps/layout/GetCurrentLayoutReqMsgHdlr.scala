package org.averox.core.apps.layout

import org.averox.common2.msgs._
import org.averox.core2.MeetingStatus2x
import org.averox.core.models.Layouts
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }

trait GetCurrentLayoutReqMsgHdlr {
  this: LayoutApp2x =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleGetCurrentLayoutReqMsg(msg: GetCurrentLayoutReqMsg): Unit = {

    def broadcastEvent(msg: GetCurrentLayoutReqMsg): Unit = {

      val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(GetCurrentLayoutRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(GetCurrentLayoutRespMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = GetCurrentLayoutRespMsgBody(
        Layouts.getCurrentLayout(liveMeeting.layouts),
        Layouts.getLayoutSetter(liveMeeting.layouts)
      )
      val event = GetCurrentLayoutRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      outGW.send(msgEvent)
    }

    broadcastEvent(msg)
  }
}
