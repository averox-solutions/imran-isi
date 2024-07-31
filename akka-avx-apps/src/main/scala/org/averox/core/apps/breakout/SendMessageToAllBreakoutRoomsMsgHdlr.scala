package org.averox.core.apps.breakout

import org.averox.common2.msgs._
import org.averox.core.api.SendMessageToBreakoutRoomInternalMsg
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.bus.AveroxEvent
import org.averox.core.db.NotificationDAO
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.RegisteredUsers
import org.averox.core.running.{ MeetingActor, OutMsgRouter }
import org.averox.core2.message.senders.MsgBuilder

trait SendMessageToAllBreakoutRoomsMsgHdlr extends RightsManagementTrait {
  this: MeetingActor =>

  val outGW: OutMsgRouter

  def handleSendMessageToAllBreakoutRoomsMsg(msg: SendMessageToAllBreakoutRoomsReqMsg, state: MeetingState2x): MeetingState2x = {
    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to send message to all breakout rooms for meeting."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
      state
    } else {
      for {
        breakoutModel <- state.breakout
        senderUser <- RegisteredUsers.findWithUserId(msg.header.userId, liveMeeting.registeredUsers)
      } yield {
        breakoutModel.rooms.values.foreach { room =>
          eventBus.publish(AveroxEvent(room.id, SendMessageToBreakoutRoomInternalMsg(props.breakoutProps.parentId, room.id, senderUser.name, msg.body.msg)))
        }

        val event = buildSendMessageToAllBreakoutRoomsEvtMsg(msg.header.userId, msg.body.msg, breakoutModel.rooms.size)
        outGW.send(event)

        val notifyUserEvent = MsgBuilder.buildNotifyUserInMeetingEvtMsg(
          msg.header.userId,
          liveMeeting.props.meetingProp.intId,
          "info",
          "group_chat",
          "app.createBreakoutRoom.msgToBreakoutsSent",
          "Message for chat sent successfully",
          Vector(s"${breakoutModel.rooms.size}")
        )
        outGW.send(notifyUserEvent)
        NotificationDAO.insert(notifyUserEvent)

        log.debug("Sending message '{}' to all breakout rooms in meeting {}", msg.body.msg, props.meetingProp.intId)
      }

      state
    }
  }

  def buildSendMessageToAllBreakoutRoomsEvtMsg(senderId: String, msg: String, totalOfRooms: Int): BbbCommonEnvCoreMsg = {
    val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
    val envelope = BbbCoreEnvelope(SendMessageToAllBreakoutRoomsEvtMsg.NAME, routing)
    val header = BbbClientMsgHeader(SendMessageToAllBreakoutRoomsEvtMsg.NAME, liveMeeting.props.meetingProp.intId, "not-used")

    val body = SendMessageToAllBreakoutRoomsEvtMsgBody(props.meetingProp.intId, senderId, msg, totalOfRooms)
    val event = SendMessageToAllBreakoutRoomsEvtMsg(header, body)
    BbbCommonEnvCoreMsg(envelope, event)
  }

}
