package org.averox.core.apps.meeting

import org.averox.common2.msgs.ValidateConnAuthTokenSysMsg
import org.averox.core.models.RegisteredUsers
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.message.senders.MsgBuilder

trait ValidateConnAuthTokenSysMsgHdlr {
  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleValidateConnAuthTokenSysMsg(msg: ValidateConnAuthTokenSysMsg): Unit = {
    val regUser = RegisteredUsers.getRegisteredUserWithToken(
      msg.body.authToken,
      msg.body.userId,
      liveMeeting.registeredUsers
    )

    regUser match {
      case Some(u) =>
        val event = MsgBuilder.buildValidateConnAuthTokenSysRespMsg(msg.body.meetingId, msg.body.userId,
          true, msg.body.connId, msg.body.app)
        outGW.send(event)
      case None =>
        val event = MsgBuilder.buildValidateConnAuthTokenSysRespMsg(msg.body.meetingId, msg.body.userId,
          false, msg.body.connId, msg.body.app)
        outGW.send(event)
    }
  }
}
