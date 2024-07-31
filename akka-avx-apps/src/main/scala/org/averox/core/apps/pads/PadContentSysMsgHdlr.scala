package org.averox.core.apps.pads

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.db.{ SharedNotesRevDAO }
import org.averox.core.models.Pads
import org.averox.core.running.LiveMeeting

trait PadContentSysMsgHdlr {
  this: PadsApp2x =>

  def handle(msg: PadContentSysMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    def broadcastEvent(externalId: String, padId: String, rev: String, start: Int, end: Int, text: String): Unit = {
      val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
      val envelope = BbbCoreEnvelope(PadContentEvtMsg.NAME, routing)
      val header = BbbCoreHeaderWithMeetingId(PadContentEvtMsg.NAME, liveMeeting.props.meetingProp.intId)
      val body = PadContentEvtMsgBody(externalId, padId, rev, start, end, text)
      val event = PadContentEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      bus.outGW.send(msgEvent)
    }

    Pads.getGroupById(liveMeeting.pads, msg.body.groupId) match {
      case Some(group) => {
        SharedNotesRevDAO.update(
          liveMeeting.props.meetingProp.intId,
          group.externalId,
          msg.body.rev.toInt,
          msg.body.start,
          msg.body.end,
          msg.body.text
        )
        broadcastEvent(group.externalId, msg.body.padId, msg.body.rev, msg.body.start, msg.body.end, msg.body.text)
      }
      case _ =>
    }
  }
}
