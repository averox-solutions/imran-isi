package org.averox.core.apps.pads

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.db.SharedNotesSessionDAO
import org.averox.core.models.Pads
import org.averox.core.running.LiveMeeting

trait PadSessionDeletedSysMsgHdlr {
  this: PadsApp2x =>

  def handle(msg: PadSessionDeletedSysMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastEvent(externalId: String, userId: String, sessionId: String): Unit = {
      val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
      val envelope = BbbCoreEnvelope(PadSessionDeletedEvtMsg.NAME, routing)
      val header = BbbCoreHeaderWithMeetingId(PadSessionDeletedEvtMsg.NAME, liveMeeting.props.meetingProp.intId)
      val body = PadSessionDeletedEvtMsgBody(externalId, userId, sessionId)
      val event = PadSessionDeletedEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      bus.outGW.send(msgEvent)
    }

    Pads.getGroupById(liveMeeting.pads, msg.body.groupId) match {
      case Some(group) => {
        SharedNotesSessionDAO.delete(liveMeeting.props.meetingProp.intId, msg.body.userId, msg.body.sessionId)
        broadcastEvent(group.externalId, msg.body.userId, msg.body.sessionId)
      }
      case _ =>
    }
  }
}
