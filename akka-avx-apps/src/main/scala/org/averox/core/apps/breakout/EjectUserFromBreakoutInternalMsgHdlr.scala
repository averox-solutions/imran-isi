package org.averox.core.apps.breakout

import org.averox.core.api.EjectUserFromBreakoutInternalMsg
import org.averox.core.apps.users.UsersApp
import org.averox.core.db.{ BreakoutRoomUserDAO, UserDAO }
import org.averox.core.models.RegisteredUsers
import org.averox.core.running.{ LiveMeeting, MeetingActor, OutMsgRouter }
import org.averox.core2.message.senders.Sender

trait EjectUserFromBreakoutInternalMsgHdlr {
  this: MeetingActor =>

  val liveMeeting: LiveMeeting

  val outGW: OutMsgRouter

  def handleEjectUserFromBreakoutInternalMsgHdlr(msg: EjectUserFromBreakoutInternalMsg) = {

    for {
      registeredUser <- RegisteredUsers.findAllWithExternUserId(msg.extUserId, liveMeeting.registeredUsers)
    } yield {
      UsersApp.ejectUserFromMeeting(
        outGW,
        liveMeeting,
        registeredUser.id,
        msg.ejectedBy,
        msg.reason,
        msg.reasonCode,
        msg.ban
      )

      //TODO inform reason
      UserDAO.softDelete(registeredUser.meetingId, registeredUser.id)

      // send a system message to force disconnection
      Sender.sendDisconnectClientSysMsg(msg.breakoutId, registeredUser.id, msg.ejectedBy, msg.reasonCode, outGW)

      // Force reconnection with graphql to refresh permissions
      Sender.sendForceUserGraphqlReconnectionSysMsg(liveMeeting.props.meetingProp.intId, registeredUser.id, registeredUser.sessionToken, msg.reasonCode, outGW)

      //send users update to parent meeting
      BreakoutHdlrHelpers.updateParentMeetingWithUsers(liveMeeting, eventBus)

      log.info("Eject user {} id={} in breakoutId {}", registeredUser.name, registeredUser.id, msg.breakoutId)
    }

  }
}
