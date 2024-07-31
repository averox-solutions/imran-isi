package org.averox.core.apps.users

import org.averox.common2.msgs._
import org.averox.core.apps.RightsManagementTrait
import org.averox.core.apps.groupchats.GroupChatApp
import org.averox.core.db.ChatMessageDAO
import org.averox.core.models.{ GroupChatFactory, GroupChatMessage, Roles, UserState, Users2x }
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.MeetingStatus2x
import org.averox.core2.message.senders.MsgBuilder

trait ChangeUserAwayReqMsgHdlr extends RightsManagementTrait {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleChangeUserAwayReqMsg(msg: ChangeUserAwayReqMsg): Unit = {
    log.info("handleChangeUserAwayReqMsg: away={} userId={}", msg.body.away, msg.body.userId)

    def broadcast(user: UserState, away: Boolean): Unit = {
      val routingChange = Routing.addMsgToClientRouting(
        MessageTypes.BROADCAST_TO_MEETING,
        liveMeeting.props.meetingProp.intId, user.intId
      )
      val envelopeChange = BbbCoreEnvelope(UserAwayChangedEvtMsg.NAME, routingChange)
      val headerChange = BbbClientMsgHeader(UserAwayChangedEvtMsg.NAME, liveMeeting.props.meetingProp.intId,
        user.intId)

      val bodyChange = UserAwayChangedEvtMsgBody(user.intId, away)
      val eventChange = UserAwayChangedEvtMsg(headerChange, bodyChange)
      val msgEventChange = BbbCommonEnvCoreMsg(envelopeChange, eventChange)
      outGW.send(msgEventChange)
    }

    val permissions = MeetingStatus2x.getPermissions(liveMeeting.status)

    for {
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.body.userId)
      newUserState <- Users2x.setUserAway(liveMeeting.users2x, user.intId, msg.body.away)
    } yield {
      val msgMeta = Map(
        "away" -> msg.body.away
      )

      if (!(user.role == Roles.VIEWER_ROLE && user.locked && permissions.disablePubChat)
        && ((user.away && !msg.body.away) || (!user.away && msg.body.away))) {
        ChatMessageDAO.insertSystemMsg(liveMeeting.props.meetingProp.intId, GroupChatApp.MAIN_PUBLIC_CHAT, "", GroupChatMessageType.USER_AWAY_STATUS_MSG, msgMeta, user.name)
      }

      broadcast(newUserState, msg.body.away)
    }
  }
}