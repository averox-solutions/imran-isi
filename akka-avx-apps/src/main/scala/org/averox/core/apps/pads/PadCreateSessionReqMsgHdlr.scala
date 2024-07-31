package org.averox.core.apps.pads

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.models.Pads
import org.averox.core.running.LiveMeeting

trait PadCreateSessionReqMsgHdlr {
  this: PadsApp2x =>

  def handle(msg: PadCreateSessionReqMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastEvent(groupId: String, userId: String): Unit = {
      val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
      val envelope = BbbCoreEnvelope(PadCreateSessionCmdMsg.NAME, routing)
      val header = BbbCoreHeaderWithMeetingId(PadCreateSessionCmdMsg.NAME, liveMeeting.props.meetingProp.intId)
      val body = PadCreateSessionCmdMsgBody(groupId, userId)
      val event = PadCreateSessionCmdMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      bus.outGW.send(msgEvent)
    }

    if (Pads.hasAccess(liveMeeting, msg.body.externalId, msg.header.userId)) {
      Pads.getGroup(liveMeeting.pads, msg.body.externalId) match {
        case Some(group) => broadcastEvent(group.groupId, msg.header.userId)
        case _           =>
      }
    }
  }
}
