package org.averox.core.apps.pads

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.models.Pads
import org.averox.core.running.LiveMeeting

trait PadGroupCreatedEvtMsgHdlr {
  this: PadsApp2x =>

  def handle(msg: PadGroupCreatedEvtMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    Pads.getGroup(liveMeeting.pads, msg.body.externalId) match {
      case Some(group) => {
        Pads.setGroupId(liveMeeting.pads, msg.body.externalId, msg.body.groupId)

        //Group was created, now request to create the pad
        PadslHdlrHelpers.broadcastPadCreateCmdMsg(bus.outGW, liveMeeting.props.meetingProp.intId, msg.body.groupId, msg.body.externalId)
      }
      case _ =>
    }
  }
}
