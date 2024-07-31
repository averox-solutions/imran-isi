package org.averox.core.apps.pads

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.db.{ SharedNotesRevDAO }
import org.averox.core.models.Pads
import org.averox.core.running.LiveMeeting

trait PadUpdatedSysMsgHdlr {
  this: PadsApp2x =>

  def handle(msg: PadUpdatedSysMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastEvent(externalId: String, padId: String, userId: String, rev: Int, changeset: String): Unit = {
      val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
      val envelope = BbbCoreEnvelope(PadUpdatedEvtMsg.NAME, routing)
      val header = BbbCoreHeaderWithMeetingId(PadUpdatedEvtMsg.NAME, liveMeeting.props.meetingProp.intId)
      val body = PadUpdatedEvtMsgBody(externalId, padId, userId, rev, changeset)
      val event = PadUpdatedEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      bus.outGW.send(msgEvent)
    }

    Pads.getGroupById(liveMeeting.pads, msg.body.groupId) match {
      case Some(group) => {
        Pads.setRev(liveMeeting.pads, group.externalId, msg.body.rev)
        SharedNotesRevDAO.insert(liveMeeting.props.meetingProp.intId, group.externalId, msg.body.rev, msg.body.userId, msg.body.changeset)
        broadcastEvent(group.externalId, msg.body.padId, msg.body.userId, msg.body.rev, msg.body.changeset)
      }
      case _ =>
    }
  }
}
