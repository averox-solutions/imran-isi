package org.averox.core.apps.pads

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.db.SharedNotesDAO
import org.averox.core.models.Pads
import org.averox.core.running.LiveMeeting

trait PadCreatedEvtMsgHdlr {
  this: PadsApp2x =>

  def handle(msg: PadCreatedEvtMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastEvent(externalId: String, userId: String, padId: String, name: String): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, userId)
      val envelope = BbbCoreEnvelope(PadCreatedRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(PadCreatedRespMsg.NAME, liveMeeting.props.meetingProp.intId, userId)
      val body = PadCreatedRespMsgBody(externalId, padId, name)
      val event = PadCreatedRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      bus.outGW.send(msgEvent)
    }

    Pads.getGroupById(liveMeeting.pads, msg.body.groupId) match {
      case Some(group) => {
        Pads.setPadId(liveMeeting.pads, group.externalId, msg.body.padId)
        SharedNotesDAO.insert(liveMeeting.props.meetingProp.intId, group, msg.body.padId, msg.body.name)
        broadcastEvent(group.externalId, group.userId, msg.body.padId, msg.body.name)
      }
      case _ =>
    }
  }
}
