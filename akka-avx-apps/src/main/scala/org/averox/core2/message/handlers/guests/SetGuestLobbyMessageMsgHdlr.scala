package org.averox.core2.message.handlers.guests

import org.averox.common2.msgs.SetGuestLobbyMessageCmdMsg
import org.averox.core.models.GuestsWaiting
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.message.senders.MsgBuilder
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.db.MeetingUsersPoliciesDAO
import org.averox.core.running.MeetingActor
import org.averox.core.util.HtmlUtil.htmlToHtmlEntities

trait SetGuestLobbyMessageMsgHdlr extends RightsManagementTrait {
  this: MeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleSetGuestLobbyMessageMsg(msg: SetGuestLobbyMessageCmdMsg): Unit = {
    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to set guest lobby message in meeting."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
    } else {
      val sanitizedMessage = htmlToHtmlEntities(msg.body.message)
      GuestsWaiting.setGuestLobbyMessage(liveMeeting.guestsWaiting, sanitizedMessage)
      MeetingUsersPoliciesDAO.updateGuestLobbyMessage(liveMeeting.props.meetingProp.intId, sanitizedMessage)
      val event = MsgBuilder.buildGuestLobbyMessageChangedEvtMsg(
        liveMeeting.props.meetingProp.intId,
        msg.header.userId,
        sanitizedMessage
      )
      outGW.send(event)
    }
  }
}
