package org.averox.core2.message.handlers.guests

import org.averox.common2.msgs.SetPrivateGuestLobbyMessageCmdMsg
import org.averox.core.models.GuestsWaiting
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.message.senders.MsgBuilder
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.db.UserStateDAO
import org.averox.core.running.MeetingActor
import org.averox.core.util.HtmlUtil.htmlToHtmlEntities

trait SetPrivateGuestLobbyMessageCmdMsgHdlr extends RightsManagementTrait {
  this: MeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleSetPrivateGuestLobbyMessageCmdMsg(msg: SetPrivateGuestLobbyMessageCmdMsg): Unit = {
    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to send private guest lobby messages."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
    } else {
      val sanitizedMessage = htmlToHtmlEntities(msg.body.message)
      GuestsWaiting.setPrivateGuestLobbyMessage(liveMeeting.guestsWaiting, msg.body.guestId, sanitizedMessage)
      UserStateDAO.updateGuestLobbyMessage(msg.header.meetingId, msg.body.guestId, sanitizedMessage)
      val event = MsgBuilder.buildPrivateGuestLobbyMsgChangedEvtMsg(
        liveMeeting.props.meetingProp.intId,
        msg.header.userId,
        msg.body.guestId,
        sanitizedMessage
      )
      outGW.send(event)
    }
  }
}
