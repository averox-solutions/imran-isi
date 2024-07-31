package org.averox.core.apps.pads

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.db.SharedNotesSessionDAO
import org.averox.core.models.Pads
import org.averox.core.running.LiveMeeting

trait PadSessionCreatedEvtMsgHdlr {
  this: PadsApp2x =>

  def handle(msg: PadSessionCreatedEvtMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastEvent(externalId: String, userId: String, sessionId: String): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, userId)
      val envelope = BbbCoreEnvelope(PadSessionCreatedRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(PadSessionCreatedRespMsg.NAME, liveMeeting.props.meetingProp.intId, userId)
      val body = PadSessionCreatedRespMsgBody(externalId, sessionId)
      val event = PadSessionCreatedRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      bus.outGW.send(msgEvent)
    }

    Pads.getGroupById(liveMeeting.pads, msg.body.groupId) match {
      case Some(group) => {
        SharedNotesSessionDAO.insert(liveMeeting.props.meetingProp.intId, group.externalId, msg.body.userId, msg.body.sessionId)
        broadcastEvent(group.externalId, msg.body.userId, msg.body.sessionId)
      }
      case _ =>
    }
  }
}
