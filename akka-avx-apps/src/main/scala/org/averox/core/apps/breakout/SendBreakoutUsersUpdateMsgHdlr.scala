package org.averox.core.apps.breakout

import org.averox.core.api.{ SendBreakoutUsersAuditInternalMsg }
import org.averox.core.running.{ MeetingActor, OutMsgRouter }

trait SendBreakoutUsersUpdateMsgHdlr {
  this: MeetingActor =>

  val outGW: OutMsgRouter

  def handleSendBreakoutUsersUpdateInternalMsg(msg: SendBreakoutUsersAuditInternalMsg): Unit = {

    BreakoutHdlrHelpers.updateParentMeetingWithUsers(
      liveMeeting,
      eventBus
    )
  }
}
