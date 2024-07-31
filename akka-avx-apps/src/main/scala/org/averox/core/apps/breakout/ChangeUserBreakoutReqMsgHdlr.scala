package org.averox.core.apps.breakout

import org.averox.common2.msgs._
import org.averox.core.api.EjectUserFromBreakoutInternalMsg
import org.averox.core.apps.breakout.BreakoutHdlrHelpers.getRedirectUrls
import org.averox.core.apps.{PermissionCheck, RightsManagementTrait}
import org.averox.core.bus.AveroxEvent
import org.averox.core.db.{BreakoutRoomUserDAO, NotificationDAO}
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.EjectReasonCode
import org.averox.core.running.{MeetingActor, OutMsgRouter}
import org.averox.core2.message.senders.MsgBuilder

trait ChangeUserBreakoutReqMsgHdlr extends RightsManagementTrait {
  this: MeetingActor =>

  val outGW: OutMsgRouter

  def handleChangeUserBreakoutReqMsg(msg: ChangeUserBreakoutReqMsg, state: MeetingState2x): MeetingState2x = {

    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to move user among breakout rooms."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
      state
    } else {
      val meetingId = liveMeeting.props.meetingProp.intId

      for {
        breakoutModel <- state.breakout
      } yield {
        //Eject user from room From
        for {
          roomFrom <- breakoutModel.rooms.get(msg.body.fromBreakoutId)
        } yield {
          roomFrom.users.filter(u => u.id == msg.body.userId + "-" + roomFrom.sequence).foreach(user => {
            eventBus.publish(AveroxEvent(roomFrom.id, EjectUserFromBreakoutInternalMsg(meetingId, roomFrom.id, user.id, msg.header.userId, "User moved to another room", EjectReasonCode.EJECT_USER, false)))
          })
        }

        val isSameRoom = msg.body.fromBreakoutId == msg.body.toBreakoutId
        val removePreviousRoomFromDb = !breakoutModel.rooms.exists(r => r._2.freeJoin) && !isSameRoom

        //Get join URL for room To
        val redirectToHtml5JoinURL = (
            for {
              roomTo <- breakoutModel.rooms.get(msg.body.toBreakoutId)
              (redirectToHtml5JoinURL, redirectJoinURL) <- getRedirectUrls(liveMeeting, msg.body.userId, roomTo.externalId, roomTo.sequence.toString())
            } yield redirectToHtml5JoinURL
          ).getOrElse("")

        BreakoutHdlrHelpers.sendChangeUserBreakoutMsg(
          outGW,
          meetingId,
          msg.body.userId,
          msg.body.fromBreakoutId,
          msg.body.toBreakoutId,
          redirectToHtml5JoinURL,
        )

        //Update database
        BreakoutRoomUserDAO.updateRoomChanged(
          meetingId,
          msg.body.userId,
          msg.body.fromBreakoutId,
          msg.body.toBreakoutId,
          redirectToHtml5JoinURL,
          removePreviousRoomFromDb)

        //Send notification to moved User
        for {
          roomFrom <- breakoutModel.rooms.get(msg.body.fromBreakoutId)
          roomTo <- breakoutModel.rooms.get(msg.body.toBreakoutId)
        } yield {
          val notifyUserEvent = MsgBuilder.buildNotifyUserInMeetingEvtMsg(
            msg.body.userId,
            liveMeeting.props.meetingProp.intId,
            "info",
            "promote",
            "app.updateBreakoutRoom.userChangeRoomNotification",
            "Notification to warn user was moved to another room",
            Vector(roomTo.shortName)
          )
          outGW.send(notifyUserEvent)
          NotificationDAO.insert(notifyUserEvent)
        }
      }

      state
    }
  }

}