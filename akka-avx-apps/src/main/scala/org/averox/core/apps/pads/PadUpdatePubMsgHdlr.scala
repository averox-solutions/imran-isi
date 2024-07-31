package org.averox.core.apps.pads

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.models.Pads
import org.averox.core.running.LiveMeeting

trait PadUpdatePubMsgHdlr {
  this: PadsApp2x =>

  def handle(msg: PadUpdatePubMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastEvent(groupId: String, name: String, text: String): Unit = {
      val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
      val envelope = BbbCoreEnvelope(PadUpdateCmdMsg.NAME, routing)
      val header = BbbCoreHeaderWithMeetingId(PadUpdateCmdMsg.NAME, liveMeeting.props.meetingProp.intId)
      val body = PadUpdateCmdMsgBody(groupId, name, text)
      val event = PadUpdateCmdMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      bus.outGW.send(msgEvent)
    }

    if (Pads.hasAccess(liveMeeting, msg.body.externalId, msg.header.userId) || msg.body.transcript == true) {
      Pads.getGroup(liveMeeting.pads, msg.body.externalId) match {
        case Some(group) => broadcastEvent(group.groupId, msg.body.externalId, msg.body.text)
        case _           =>
      }
    }
  }
}
