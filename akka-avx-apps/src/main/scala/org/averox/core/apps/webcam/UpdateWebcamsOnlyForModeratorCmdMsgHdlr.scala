package org.averox.core.apps.webcam

import org.averox.common2.msgs._
import org.averox.core.apps.PermissionCheck
import org.averox.core.bus.MessageBus
import org.averox.core.db.{ MeetingUsersPoliciesDAO, NotificationDAO }
import org.averox.core.models.{ RegisteredUsers, Roles, Users2x }
import org.averox.core.running.LiveMeeting
import org.averox.core2.message.senders.{ MsgBuilder, Sender }

trait UpdateWebcamsOnlyForModeratorCmdMsgHdlr {
  this: WebcamApp2x =>

  def handle(
      msg:         UpdateWebcamsOnlyForModeratorCmdMsg,
      liveMeeting: LiveMeeting,
      bus:         MessageBus
  ) {
    val meetingId = liveMeeting.props.meetingProp.intId

    def broadcastEvent(meetingId: String, userId: String, webcamsOnlyForModerator: Boolean) {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, meetingId, userId)
      val envelope = BbbCoreEnvelope(WebcamsOnlyForModeratorChangedEvtMsg.NAME, routing)
      val body = WebcamsOnlyForModeratorChangedEvtMsgBody(webcamsOnlyForModerator, userId)
      val header = BbbClientMsgHeader(WebcamsOnlyForModeratorChangedEvtMsg.NAME, meetingId, userId)
      val event = WebcamsOnlyForModeratorChangedEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      bus.outGW.send(msgEvent)
    }

    val allow = CameraHdlrHelpers.isWebcamsOnlyForModeratorUpdateAllowed(
      liveMeeting,
      msg.header.userId
    )

    if (!allow) {
      val reason = "No permission to change lock settings"
      PermissionCheck.ejectUserForFailedPermission(
        meetingId,
        msg.header.userId,
        reason,
        bus.outGW,
        liveMeeting
      )
    } else {
      CameraHdlrHelpers.updateWebcamsOnlyForModerator(
        liveMeeting,
        msg.body.webcamsOnlyForModerator,
        bus.outGW
      ) match {
          case Some(value) => {
            log.info(s"Change webcams only for moderator status. meetingId=${meetingId} value=${value}")

            MeetingUsersPoliciesDAO.updateWebcamsOnlyForModerator(meetingId, msg.body.webcamsOnlyForModerator)

            if (value) {
              val notifyEvent = MsgBuilder.buildNotifyAllInMeetingEvtMsg(
                meetingId,
                "info",
                "lock",
                "app.userList.userOptions.webcamsOnlyForModerator",
                "Label to disable all webcams except for the moderators cam",
                Vector()
              )
              bus.outGW.send(notifyEvent)
              NotificationDAO.insert(notifyEvent)
            } else {
              val notifyEvent = MsgBuilder.buildNotifyAllInMeetingEvtMsg(
                meetingId,
                "info",
                "lock",
                "app.userList.userOptions.enableOnlyModeratorWebcam",
                "Label to enable all webcams except for the moderators cam",
                Vector()
              )
              bus.outGW.send(notifyEvent)
              NotificationDAO.insert(notifyEvent)
            }

            broadcastEvent(meetingId, msg.body.setBy, value)

            //Refresh graphql session for all locked viewers
            for {
              user <- Users2x.findAll(liveMeeting.users2x)
              if user.locked
              if user.role == Roles.VIEWER_ROLE
              regUser <- RegisteredUsers.findWithUserId(user.intId, liveMeeting.registeredUsers)
            } yield {
              Sender.sendForceUserGraphqlReconnectionSysMsg(liveMeeting.props.meetingProp.intId, regUser.id, regUser.sessionToken, "webcamOnlyForMod_changed", bus.outGW)
            }
          }
          case _ =>
        }
    }
  }
}
